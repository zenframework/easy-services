//
///- REQUIRE FN
// equivalent to require from node.js
function require(url) {
	if (url.toLowerCase().substr(-3) !== '.js')
		url += '.js'; // to allow loading without js suffix;
	if (!require.stack)
		require.stack = [];
	if (!require.cache)
		require.cache = []; // init cache
	// check if require call stack already contains url
	var deadlock = require.stack.indexOf(url) >= 0;
	require.stack.push(url); // add url to require call stack
	if (deadlock)
		throw 'require() deadlock: ' + require.stack;
	var exports = require.cache[url]; // get from cache
	if (!exports) { // not cached
		exports = {};
		var X = new XMLHttpRequest();
		X.open('GET', url, 0); // sync
		X.send();
		if (X.status && X.status !== 200)
			throw new Error('Error loading module ' + url + ': ' + X.statusText);
		var source = X.responseText;
		// fix (if saved form for Chrome Dev Tools)
		if (source.substr(0, 10) === '(function(') {
			var moduleStart = source.indexOf('{');
			var moduleEnd = source.lastIndexOf('})');
			var CDTcomment = source.indexOf('//@ ');
			if (CDTcomment > -1 && CDTcomment < moduleStart + 6)
				moduleStart = source.indexOf('\n', CDTcomment);
			source = source.slice(moduleStart + 1, moduleEnd - 1);
		}
		// fix, add comment to show source on Chrome Dev Tools
		source = url + '\n' + source;
		if (url.indexOf('/') !== 0)
			source = window.location.pathname + source;
		source = '//# sourceURL=' + window.location.origin + source;
		// according to node.js modules
		var module = {
			id : url,
			uri : url,
			exports : exports
		};
		// create a Fn with module code, and 3 params: require, exports & module
		var anonFn = new Function('require', 'exports', 'module', source);
		// call the Fn, Execute the module
		anonFn(require, exports, module);
		// cache obj exported by module
		require.cache[url] = exports = module.exports;
	}
	require.stack.pop(); // remove url from require call stack
	return exports; // require returns object exported by module
}
// /- END REQUIRE FN
