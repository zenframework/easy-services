load('lib/lodash/lodash.js')
load('lib/object-model/object-model.js')
load('lib/require/require.js');

tests({

	testGetServiceProxy : function() {
		var ProxyFactory = require('generic/api/ProxyFactory');
		var model = {};
		model.Function = new Model({
			call : Model.Function(Number, Number).return(Number)
		});
		var Addition = ProxyFactory.create({
			url : 'http://localhost:10000/services/add',
			debug : false,
			service : model.Function
		});
		var result;
		Addition.call(1, 2, function(res) {
			result = res;
		});
		org.zenframework.easyservices.js.env.Environment.join();
		jsAssert.assertIntegerEquals(3, result.response);
	}

});
