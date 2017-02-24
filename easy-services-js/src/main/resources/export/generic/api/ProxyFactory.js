var DateUtil = require('generic/util/DateUtil');

var parseResponse = function(client, xhr) {
	console.log('--- ' + xhr.status);
	var success = !client.async || xhr.status === 200;
	var result = xhr.responseText ? client.parser(xhr.responseText) : {
		message : 'Ошибка соединения с сервером'
	};
	if (client.debug) {
		if (success)
			console.info('CALL', client.url, client.method, args, ':', result);
		else
			console.error('CALL', client.url, client.method, args, ':', result);
	}
	if (success && client.returns) {
		try {
			result = new client.returns(result);
		} catch (e) {
			console.error('RETURN', e);
			result = e;
			success = false;
		}
	}
	return {
		success : success,
		status : xhr.status,
		statusText : xhr.statusText,
		result : result
	};
};

var Client = function(config) {

	_.assign(this, config);

	this.call = function() {
		var me = this;
		var async = config.async;
		var len = arguments.length;
		var args = _.slice(arguments, 0, async ? len - 1 : len);
		var callback = arguments[len - 1];
		var url = me.url + '?method=' + me.method;
		var argsStr = JSON.stringify(args);
		if (!_.isEmpty(args))
			url += '&args=' + argsStr;
		var xhr = new XMLHttpRequest();
		if (async) {
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4) {
					var response = parseResponse(me, xhr);
					if (typeof callback === 'function')
						callback = {
							context : this,
							callback : callback
						};
					if (me.delay)
						setTimeout(function() {
							callback.callback.call(callback.context, response);
						}, me.delay);
					else
						callback.callback.call(callback.context, response);
				}
			};
		}
		xhr.open('GET', url, async);
		xhr.send();
		if (!async) {
			var response = parseResponse(me, xhr);
			if (response.success)
				return response.result;
			else
				throw response.result;
		}
	};
	
	_.bindAll(this, 'call');

};

var ProxyConfig = new Model({
	url : String,
	async : Boolean,
	service : Model,
	debug : Boolean,
	delay : Number,
	parser : Model.Function(String).return([Number, String, Boolean, Object, null, undefined])
}).defaults({
	async : true,
	debug : false,
	delay : 0,
	parser : function(str) {
		return JSON.parse(str, function(k, v) {
			if (DateUtil.isDate(v))
				return DateUtil.toDate(v);
			return v;
		})
	}
});

var Callback = new Model({
	context : Object,
	callback : Function
});

var ProxyFactory = {

	create : function(config) {

		config = new ProxyConfig(config);
		var service = { url : config.url };
		var serviceDef = config.service.definition;
		for (var method in serviceDef) {
			if (config.async) {
				var methodDef = serviceDef[method].definition;
				var returns = methodDef.return;
				// If proxy is async, append response callback to method model
				methodDef.arguments.push([Callback, Function]);
				// If proxy is async, method must return void
				methodDef.return = new Model();
			}
			var client = new Client({
				url : config.url,
				async : config.async,
				debug : config.debug,
				delay : config.delay,
				parser : config.parser,
				method : method,
				returns : returns
			});
			service[method] = client.call;
		}
		return new config.service(service);
	
	}

};

ProxyFactory.Callback = Callback;

module.exports = ProxyFactory;
