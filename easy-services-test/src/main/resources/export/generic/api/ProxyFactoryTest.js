load('lib/lodash/lodash.js')
load('lib/object-model/object-model.js')
load('lib/require/require.js');

var model = {
	Function : new Model({
		call : Model.Function(Number, Number).return(Number)
	})
};

var config = {
	url : 'http://localhost:10000/services/add',
	debug : false,
	service : model.Function
};

tests({

	testGetServiceProxySync : function() {
		var ProxyFactory = require('generic/api/ProxyFactory');
		var Addition = ProxyFactory.create(_.assign(config, { async : false }));
		jsAssert.assertIntegerEquals(3, Addition.call(1, 2));
	},

	testGetServiceProxyAsync : function() {
		var ProxyFactory = require('generic/api/ProxyFactory');
		var Addition = ProxyFactory.create(_.assign(config, { async : true }));
		var response;
		Addition.call(1, 2, function(res) {
			response = res;
		});
		org.zenframework.easyservices.js.env.Environment.join();
		jsAssert.assertIntegerEquals(3, response.result);
	}

});
