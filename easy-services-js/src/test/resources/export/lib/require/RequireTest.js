load('lib/require/require.js');

tests({

	testRequire : function() {
		var SimpleModule = require('SimpleModule');
		assert.assertEquals(SimpleModule.name, 'SimpleModule');
		jsAssert.assertIntegerEquals(SimpleModule.value, 1);
	}

});
