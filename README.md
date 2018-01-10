# Domino Scheduled Tasks

This is a demo of a preferred solution for re-using XPages code for scheduled tasks.

## The Issues

### LotusScript Agents
These require coding in LotusScript. That poses a few issues:

- Some aspects (e.g. calling REST services) cannot natively be done in LotusScript. There are options like LS2J, but they are not ideal.
- It requires retaining knowledge of LotusScript in addition to newer technologies. Based on personal experience, when LS is used primarily for Agents alone, muscle memory gets "flabby" resulting in basic errors and increased time to remember how to do certain elements.
- XPages code needs translating into LotusScript and maintaining in two places.
- Code cannot easily be shared across two NSFs.

### Java Agents
Here are some of the issues:

- XPages code needs copying and pasting, possibly modifying it.
- OSGi libraries cannot be used.
- Jar files have a history of memory leakage.
- Code cannot easily be shared across two NSFs.

### DOTS
DOTS works well if you wish to store the code outside the NSF. However, issues are:

- Scheduling is less straightforward.
- It uses a different area of OSGi, so some libraries available for XPages or OSGi plugin code may not be available.
- It's not widely used, so community support is limited.
- XPages code needs copying out of the NSF.
- AFAIK, DOTS cannot be called from XPages.

### XOTS
Xots works well for background tasks. With minor changes (to handle access to scopes) XPages code can be re-used. However, scheduling the code is difficult. A user-triggered function (e.g. a button on an XPage) can add it to the scheduler with a specific interval. But I'm not aware of a way to remove a tasklet from the scheduler, refresh the tasklet (if required) or easily access the schedule.

Schedules could be stored in a design note, e.g. the Icon or a properties file. But iterating all NSFs at server start in order to gather the schedule is slow. Changing a schedule or updating it via a design refresh is difficult - there is no easy hook. It's more difficult to have different schedules for different servers / environments. Viewing the schedules across the server is also a challenge.

Storing the tasklet name in a Scheduler Application would seem a good approach à la LEI (Lotus Enterprise Integrator). However, then the challenge is loading that tasklet when required in the context of the NSF and with access to all dependent code.

## The Preferred Technological Approach
An approach that satisfies all requirements uses three aspects:

- an XAgent, SmartNSF route Using CUSTOM strategy) or REST service designed to receive content via URL parameters or header parameters as a GET request, or as post data in a POST request.
- If asynchronous processing is required, a call to `Xots.getService().submit(new com.paulwithers.MyTask())`.
- Node-RED to perform the scheduling.

Using an XAgent, SmartNSF route or REST service allows code to be placed wherever required, at the same place as other web code that interacts with the NSF.

Using Xots allows background thread processing. Xots by default runs 10 threads. This is like amgr running 10 executives.

Node-RED:

- avoids reinventing the wheel for a scheduler
- is familiar for non-Domino web developers
- can be secured to ensure only authorised people can access it
- can run on premises or in the cloud
- can be installed easily on a local workstation for developers, even with Docker
- has a drag-and-drop GUI
- stores as JSON allowing easy import and export for migration, delivery or mass modification of e.g. URL hostname
- can connect to multiple Domino servers (because it's just pointing to a URL)
- allows basic authentication to be included with each task
- allows different scheduling for different environments
- allows chaining of tasks by sending a callback URL for the Xots task to call on completion or by letting the XAgent / SmartNSF route / REST service run synchronously and chaining in Node-RED
- for monthly / nth-day-of-the-month tasks, this can be checked in the code and quickly abort when not required
- Other Node-RED timers like [big-timer](https://flows.nodered.org/node/node-red-contrib-bigtimer) are available

Of course a different scheduler could be used. It just needs to allow storing of schedules and calling a REST service. This gives great flexibility for the developer.

Our recommendation is that IBM should focus on core features rather than building a scheduler. JavaScript developers will be familiar with NodeJS so will welcome a non-proprietary approach. Java developers undoubtedly have coding options for managing schedules. Developers and customers should be willing to step beyond IBM technology and expand their horizons. If a Domino (probably XPages) application is required, OpenNTF is a good vehicle for sharing and improving it.

## The Demo Project
The demo project contains three parts:

- an OSGi plugin that includes a REST endpoint and can auto-deploy the databases. This is dependent on ODA 3.2.1 and the update site part needs importing into an NSF.
- an ODP for the NSF that requires ODA 3.2.1 and Ext Lib. If not using the auto-deploy option, create new NSFs from the ODP to recommended filepaths are "openntfDemos/scheduleDemo.nsf" and openntfDemos/scheduleDemoArchive.nsf". If using a different filepath, locations will need amending in the json.
- the node-red-flow folder contains json for a set of Node-RED tasks to interact with the REST services. Find/Replace the server name in the json, save and import into Node-RED.

## Things to Note
If you want your task to wait for your code to complete (running synchronously) you don't need to use Xots. There may need to be changes to manage timeouts, I'm not sure.

Xots allows access to scoped variables from XAgents when extending `AbstractXotsXspRunnable` class (I'm not sure if you would). But SmartNSF doesn't use XPages, so you need to extend `AbstractXotsRunnable`.

If a Xots tasklet uses the annotation `session = Tasklet.Session.CLONE` it will use the authority passed in via the basic authentication of the Node-RED task. This can then use `Factory.getSession(SessionType.CURRENT).getCurrentDatabase()`. However, if you use `session = Tasklet.Session.NATIVE`, you cannot use `.getCurrentDatabase()`, you need to use `getDatabase()` passing the relevant database path. This could be passed as a property into the tasklet.