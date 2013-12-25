function Variant(chrom, strand, start, end, ref, obs) {
    var self = this;
    
    self.chrom = chrom;
    self.strand = strand;
    self.start = start;
    self.end = end;
    self.ref = ref;
    self.obs = obs;
}

function AnnotatedVariant(id, variant, flagged, values) {
    var self = this;
    self.id = id;
    self.variant = variant;
    self.flagged = flagged;
    self.values = values;
}

function ValueMap(key, value, level) {
    var self = this;
    self.key = key;
    self.value = value;
    self.level = level;
}

function ViewModel() {
    var self = this;

    self._chrom = ko.observable('');
    self._strand = ko.observable('+');
    self._start = ko.observable('');
    self._end= ko.observable('');
    self._ref = ko.observable('');
    self._obs = ko.observable('');
    
    self.addVariant = function() {
	if (self._start() && self._start().length > 0 
	    && self._end() && self._end().length > 0 
	    && self._ref() && self._ref().length > 0 
	    && self._obs() && self._obs().length > 0) {
	    self.showAlert(false);
	    self.variants.push(
		new Variant(self._chrom(), self._strand(), self._start(), 
			    self._end(), self._ref().toUpperCase(), self._obs().toUpperCase()));
	    self.reset();
	} else {
	    self.showAlert(true);
	}
    }


    self.chroms = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', 
		  '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y'];

    self.selectedOutputGroup = ko.observable("all-var-list-group-item");

    self.annotatedVariants = ko.observableArray([
	new AnnotatedVariant(1, new Variant('1', '+', 123456678, 123456678, 'G', 'A'), 
			     true, 
			     [ new ValueMap("Known Mutation", "No", "Low"),
			       new ValueMap("Allele Frequency", 0.0, "High"),
			       new ValueMap("PhyloP Score", 1.0, "Low"),
			       new ValueMap("Sift Score", 0.01, "High")]),
	new AnnotatedVariant(2, new Variant('12', '+', 66789, 66790, 'GA', 'A'), 
			     true, 
			     [ new ValueMap("Known Mutation", "Yes", "High")]),
	new AnnotatedVariant(3, new Variant('X', '+', 12345, 12345, 'A', 'C'), 
			     false, 
			     [ new ValueMap("Known Mutation", "No", "Low"),
			       new ValueMap("Allele Frequency", 0.0, "High"),
			       new ValueMap("PhyloP Score", 0.0, "Low"),
			       new ValueMap("Sift Score", 0.18, "Low")]),
	new AnnotatedVariant(4, new Variant('21', '+', 66748, 66758, 'A', 'CTGCC'), 
			     false, 
			     [ new ValueMap("Known Mutation", "No", "Low"),
			       new ValueMap("Allele Frequency", 0.5, "Low")]),
	new AnnotatedVariant(5, new Variant('2', '+', 22345676, 22345679, 'A', 'C'), 
			     false, 
			     [ new ValueMap("Known Mutation", "No", "Low"),
			       new ValueMap("Allele Frequency", 0.2, "Low")])]);

    self.variants = ko.observableArray([
	new Variant('9', '+', 1234556, 1234556, 'G', 'A'),
	new Variant('1', '-', 223456678, 223456679, 'GA', 'A'),
	new Variant('9', '+', 1234550, 1234556, 'GAGACTG', 'A'),
	new Variant('9', '+', 1234556, 1234556, 'G', 'ACTGCGC')]);

    self.removeVariant = function(variant) { self.variants.remove(variant) };

    self.showAlert = ko.observable(false);

    self.numTwo = ko.computed(function() {
	return 1+1;
    });

    self.numAll = ko.computed(function() {
	return self.annotatedVariants().length;
    });

    self.numInteresting = ko.computed(function() {
	var i = $.grep(self.annotatedVariants(), function(v) {
	    return v.flagged == true;
	});
	return i.length;
    });

    self.numUninteresting = ko.computed(function() {
	var u = $.grep(self.annotatedVariants(), function(v) {
	    return v.flagged == false;
	});
	return u.length;
    });

    self.setAll = function() {
	collapseAll();
	self.setSelectedOutputGroup('all-var-list-group-item');
	self.showInteresting(true);
	self.showUninteresting(true);
    }

    self.setInteresting = function() {
	collapseAll();
	self.setSelectedOutputGroup('int-var-list-group-item');
	self.showInteresting(true);
	self.showUninteresting(false);
    }

    self.setUninteresting = function() {
	collapseAll();
	self.setSelectedOutputGroup('ni-var-list-group-item');
	self.showInteresting(false);
	self.showUninteresting(true);
    }

    self.setSelectedOutputGroup = function(group) {
	self.selectedOutputGroup(group);
    }

    self.showInteresting = ko.observable(true);

    self.showUninteresting = ko.observable(true);

    self.reset = function() {
	self._chrom('');
	self._strand('+');
	self._start('');
	self._end('');
	self._ref('');
	self._obs('');
	setUpInputMasks();
    }
}

ko.applyBindings(new ViewModel());

function collapseAll() {
    $('[id^=collapse]').each(function(idx, elem) {
	if ($(elem).hasClass('in'))
	    $(elem).collapse('hide');
    });
}

function setUpInputMasks() {
    $("#startCoord").inputmask('Regex', {regex: "[0-9]*"});
    $("#endCoord").inputmask('Regex', {regex: "[0-9]*"});
    $("#refSequence").inputmask('Regex', {regex: "[ACTGactg]*"});
    $("#obsSequence").inputmask('Regex', {regex: "[ACTGactg]*"});
}

$(document).ready(function(){
    $('.collapse').collapse({toggle: false});
    setUpInputMasks();
});
