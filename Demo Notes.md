## Demo Notes

### Installation

- Install OSGi plugin
- Access via REST at /scheduled-xots-demo/setup

We have live and archive set up at openntf-demo/scheduledXotsDemo.nsf and openntf-demo/scheduledXotsDemoArchive.nsf, with 400 documents in live

### ngrok
If Domino server is not visible to internet, start ngrok. Issue command `ngrok http 80` to create a tunnel to Domino (if running on Port 80).
This will give you a http tunnel to the server, e.g. `http://1b81a6b9.ngrok.io`

### Node-Red
Set up Node-RED. To create a new Node-RED instance use command:
`docker run -it -p 1880:1880 -v C:/PaulTemp/node-red-data --name mynodered nodered/node-red-docker`
This tells it which port to run on, where to store the data, and a name to run as.

If a Node-RED instance with that name has already been setup, use `docker start mynodered`.

### Demo 1 - Basic Archive
Set up an `inject` node to trigger on schedule. You can also set it to run on start by ticking the **Inject once at start?** checkbox

Add a `http request` node to run the HTTP REST service. Point to the SchedArchive XAgent, URL will be e.g. `http://1b81a6b9.ngrok.io/openntf-demo/scheduledXotsDemo.nsf/SchedArchive.xsp`. You will need to authenticate. Use Basic Authentication and enter the username and password with access to the NSF.

Add an Output node to output to the debug console.

Link the nodes up and Deploy.

### Demo 2 - XRest Archive
Set up an `inject` node to trigger on schedule. You can also set it to run on start by ticking the **Inject once at start?** checkbox

Add a `http request` node to run the HTTP REST service. Point to the SchedArchive XAgent, URL will be e.g. `http://1b81a6b9.ngrok.io/openntf-demo/scheduledXotsDemo.nsf/xsp/.xrest/xrestArchive`. You will need to authenticate. Use Basic Authentication and enter the username and password with access to the NSF.

Add an `Output` node to output to the debug console.

Link the nodes up and Deploy.

### Demo 3 - Chain and Custom Authentication in OSGi REST Service

In a command prompt, go to ngrok folder and issue command `ngrok http 1880` to start a tunnel to Node-RED instance.


Set up an `inject` node to trigger on schedule. You can also set it to run on start by ticking the **Inject once at start?** checkbox

Add a setHeaders node to add headers to the HTTP request:

	msg.headers = {};
	msg.headers.appId = '1ugqjbq988301lb7t2oh2fsgg6';
	msg.headers.appSecret = 'pianfreuhe0u4ato38mfiibeng';
	msg.headers.redirectUrl = 'http://YOUR_SERVER_NAME/api/xpagescallback'
	return msg;

The final URL will correspond to a Node-RED endpoint.

Add a `http request` node to call a `DELETE` method to run the OSGi `doChain` REST service, e.g. http://7bf71eb5.ngrok.io/scheduled-xots-demo/doChain. The headers handle authentication - the REST service validates the appId and appSecret entered. It passes the redirectUrl to the DeletionTask runnable, which calls that URL on completion.

Add an Output node to output to the debug console.

Link the nodes up.

Add an `http input` node, for a `GET`method with URL `/api/xpagescallback`.

Add an `http request` node to run the generic REST service to populate the live database with 800 users, URL will be e.g. `http://7bf71eb5.ngrok.io/openntf-demo/scheduledXotsDemo.nsf/genericXAgent.xsp?process=loadData&userCount=800`.

Add an Output node to output to the debug console.

Link the nodes up and deploy.