var DateUtil = require('generic/util/DateUtil');

var Client = function(config) {

	_.assign(this, config);

	this.call = function() {
		var me = this;
		var len = arguments.length;
		var args = _.slice(arguments, 0, len - 1);
		var callback = arguments[len - 1];
		var url = me.url + '?method=' + me.method;
		var argsStr = JSON.stringify(args);
		if (!_.isEmpty(args))
			url += '&args=' + argsStr;
		var xhr = new XMLHttpRequest();
		xhr.open('GET', url, true);
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4) {
				var success = xhr.status === 200;
				var response = xhr.responseText ? me.parser(xhr.responseText) : {
					message : 'Ошибка соединения с сервером'
				};
				if (me.debug) {
					if (success)
						console.info('CALL', me.url, me.method, args, ':', response);
					else
						console.error('CALL', me.url, me.method, args, ':', response);
				}
				if (success && me.returns) {
					try {
						response = new me.returns(response);
					} catch (e) {
						console.error('RETURN', e);
						response = e;
						success = false;
					}
				}
				var result = {
					success : success,
					status : xhr.status,
					statusText : xhr.statusText,
					response : response
				};
				if (typeof callback === 'function')
					callback = {
						context : this,
						callback : callback
					};
				if (me.delay)
					setTimeout(function() {
						callback.callback.call(callback.context, result);
					}, me.delay);
				else
					callback.callback.call(callback.context, result);
			}
		};
		xhr.send();
	};
	
	_.bindAll(this, 'call');

};

var ProxyConfig = new Model({
	url : String,
	service : Model,
	debug : Boolean,
	delay : Number,
	parser : Model.Function(String).return([Number, String, Boolean, Object, null, undefined])
}).defaults({
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
			var methodDef = serviceDef[method].definition;
			var returns = methodDef.return;
			// Append response callback to method model
			methodDef.arguments.push([Callback, Function]);
			// Proxy is async, so method must return void
			methodDef.return = new Model();
			var client = new Client({
				url : config.url,
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
