tests({

	testRequire : function() {
		load('lib/require/require.js');
		var SimpleModule = require('SimpleModule');
		assert.assertEquals(SimpleModule.name, 'SimpleModule');
		jsAssert.assertIntegerEquals(SimpleModule.value, 1);
	}

});
