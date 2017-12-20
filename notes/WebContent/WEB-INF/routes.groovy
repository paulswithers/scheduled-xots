router.GET('xrestArchive') {
	strategy(CUSTOM) {
		javaClass('com.paulwithers.CustomRest')
	}
}