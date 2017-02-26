load('lib/lodash/lodash.js')
load('lib/object-model/object-model.js')
load('lib/require/require.js');

var ProxyFactory = require('generic/api/ProxyFactory');

var model = {};

model.SimpleBean = new Model({
	name : String,
	value : Number
});

model.Echo = new Model({
	doNothing : Model.Function().return()
});
	
model.Function = new Model({
	call : Model.Function(Number, Number).return(Number)
});
	
model.CollectionUtil = new Model({
	concat : Model.Function(Model.Array(model.SimpleBean), String).return(String),
	sortBeans : Model.Function(Model.Array(model.SimpleBean)).return(),
	sortInts : Model.Function(Model.Array(Number)).return(),
	clearBean : Model.Function(model.SimpleBean).return()
});

tests({

	testGetServiceProxySync : function() {
		var Addition = ProxyFactory.create({
			url : 'http://localhost:10000/services/add',
			async : false,
			service : model.Function
		});
		assert.assertTrue(3 == Addition.call(1, 2));
	},

	testGetServiceProxyAsync : function() {
		var Addition = ProxyFactory.create({
			url : 'http://localhost:10000/services/add',
			service : model.Function
		});
		var response;
		Addition.call(1, 2, function(res) {
			response = res;
		});
		org.zenframework.easyservices.js.env.Environment.join();
		assert.assertTrue(3 == response.result);
	},
	
	testVoidSync : function() {
		var Echo = ProxyFactory.create({
			url : 'http://localhost:10000/services/echo',
			async : false,
			service : model.Echo
		});
		Echo.doNothing();
	},
	
	testOutObjectList : function() {
		var CollectionUtil = ProxyFactory.create({
			url : 'http://localhost:10000/services/util',
			async : false,
			outParams : true,
			service : model.CollectionUtil
		});
		var list = [ { name : 'zxc', value : 3 }, { name : 'asd', value : 1 }, { name : 'qwe', value : 2 } ];
		CollectionUtil.sortBeans(list);
		assert.assertTrue('asd=1,qwe=2,zxc=3' == CollectionUtil.concat(list, ','));
	},

	testOutIntArray : function() {
		var CollectionUtil = ProxyFactory.create({
			url : 'http://localhost:10000/services/util',
			async : false,
			outParams : true,
			service : model.CollectionUtil
		});
		var list = [ 2, 3, 1 ];
		CollectionUtil.sortInts(list);
		console.log('list: ' + list);
		assert.assertEquals([1, 2, 3], list);
	},

	testOutObject : function() {
		var CollectionUtil = ProxyFactory.create({
			url : 'http://localhost:10000/services/util',
			async : false,
			outParams : true,
			service : model.CollectionUtil
		});
		var bean = { name : 'qwe', value : 1 };
		CollectionUtil.clearBean(bean);
		console.log(JSON.stringify(bean));
	}

});
