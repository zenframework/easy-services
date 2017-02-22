var ISO_DATE_REGEXP = /^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)$/;

module.exports = {

	isDate : function(str) {
		return typeof str === 'string' && ISO_DATE_REGEXP.test(str);
	},

	toDate : function(d) {
		if (d instanceof Date)
			return d;
		if (typeof d === 'number')
			return new Date(d);
		if (typeof d !== 'string')
			d = d.toString();
		// If timezone format is '(+/-)hh', ddd ':mm'
		var shiftSign = d.charAt(d.length - 3);
		if (shiftSign === '+' || shiftSign === '-')
			d += ':00';
		return new Date(d);
	}

};
