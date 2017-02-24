load('lib/require/require.js');

var SimpleModule = require('lib/require/SimpleModule');

tests({

	testRequire : function() {
		assert.assertEquals(SimpleModule.name, 'SimpleModule');
		jsAssert.assertIntegerEquals(SimpleModule.value, 1);
	}

});
