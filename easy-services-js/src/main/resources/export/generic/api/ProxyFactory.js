var DateUtil = require('generic/util/DateUtil');

var updateValues = function(oldValues, newValues) {
	if (!newValues)
		return;
	for (var i = 0; i < oldValues.length; i++) {
		var oldVal = oldValues[i];
		var newVal = newValues[i];
		var type = typeof oldVal;
		if (type === 'object') {
			for (var prop in oldVal) {
				if (oldVal.hasOwnProperty(prop))
					delete oldVal[prop];
			}
			for (var prop in newVal) {
				if (newVal.hasOwnProperty(prop))
					oldVal[prop] = newVal[prop]
			}
		} else if (type === 'array') {
			for (var i = 0; i < oldVal.length; i++)
				oldVal[i] = newVal[i];
		}
	}
};

var parseResponse = function(client, params, xhr) {

	var outParamsMode = client.outParams;

	var responseText = xhr.responseText;
	var response = responseText && !responseText.isEmpty() ? client.parser(responseText) : {
		message : 'Ошибка соединения с сервером'
	};
	if (!outParamsMode)
		response = { result : response };
	response.success = !client.async || xhr.status === 200;
	response.status = xhr.status;
	response.statusText = xhr.statusText;

	if (client.debug) {
		if (success)
			console.info('CALL', client.url, client.method, params, ':', response.result);
		else
			console.error('CALL', client.url, client.method, params, ':', response.result);
	}

	if (response.success) {
		if (typeof client.returns === 'undefined') {
			response.result = undefined;
		} else if (client.returns) {
			try {
				response.result = new client.returns(response.result);
			} catch (e) {
				console.error('RETURN', e);
				response.result = e;
				response.success = false;
			}
		}
	}

	return response;

};

var Client = function(config) {

	_.assign(this, config);

	this.call = function() {
		var me = this;
		var async = config.async;
		var outParams = me.outParams;
		var len = arguments.length;
		var params = _.slice(arguments, 0, async ? len - 1 : len);
		var callback = arguments[len - 1];
		var url = me.url + '?method=' + me.method;
		var paramsStr = JSON.stringify(params);
		if (!_.isEmpty(params))
			url += '&params=' + paramsStr;
		if (config.outParams)
			url += '&outParameters=true';
		var xhr = new XMLHttpRequest();
		if (async) {
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4) {
					var response = parseResponse(me, params, xhr);
					if (typeof callback === 'function')
						callback = {
							context : this,
							callback : callback
						};
					if (me.delay) {
						setTimeout(function() {
							if (outParams)
								updateValues(params, response.parameters);
							callback.callback.call(callback.context, response);
						}, me.delay);
					} else {
						if (outParams)
							updateValues(params, response.parameters);
						callback.callback.call(callback.context, response);
					}
				}
			};
		}
		xhr.open('GET', encodeURI(url), async);
		xhr.send();
		if (!async) {
			var response = parseResponse(me, params, xhr);
			if (outParams)
				updateValues(params, response.parameters);
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
	outParams : Boolean,
	service : Model,
	debug : Boolean,
	delay : Number,
	parser : Model.Function(String).return([Number, String, Boolean, Object, null, undefined])
}).defaults({
	async : true,
	outParams : false,
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
			if (config.async) {
				// If proxy is async, append response callback to method model
				methodDef.arguments.push([Callback, Function]);
				// If proxy is async, method must return void
				methodDef.return = undefined;
			}
			var client = new Client({
				url : config.url,
				async : config.async,
				outParams : config.outParams,
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
