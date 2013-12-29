function Input(name, owner, variants) {
    var self = this;

    self.name = name;
    self.owner = owner;
    self.variants = variants;
}

function Variant(chromosome, strand, start, end, reference, observed) {
    var self = this;
    
    self.chromosome = chromosome;
    self.strand = strand;
    self.start = start;
    self.end = end;
    self.reference = reference;
    self.observed = observed;
}

function AnnotatedVariant(id, variant, flagged, valueEntries) {
    var self = this;
    self.variant = variant;
    self.flagged = flagged;
    self.valueEntries = valueEntries;
}

function ValueEntry(key, value, level) {
    var self = this;
    self.key = key;
    self.value = value;
    self.level = level;
}

function ViewModel() {
    var self = this;

    self._chromosome = ko.observable('');
    self._strand = ko.observable('+');
    self._start = ko.observable('');
    self._end= ko.observable('');
    self._reference = ko.observable('');
    self._observed = ko.observable('');
    self.loading = ko.observable(false);
    
    self.addVariant = function() {
	if (self._start() && self._start().length > 0 
	    && self._end() && self._end().length > 0 
	    && self._reference() && self._reference().length > 0 
	    && self._observed() && self._observed().length > 0) {
	    self.showAlert(false);
	    self.variants.push(
		new Variant(self._chromosome(), self._strand(), self._start(), 
			    self._end(), self._reference().toUpperCase(), self._observed().toUpperCase()));
	    self.reset();
	} else {
	    self.showAlert(true);
	}
    }

    self.submitVariants = function() {
	if (self.variants().length == 0) {
	    alert('Add at least one variant');
	}
	else {
	    $("#submitButton").button('loading');
	    self.annotatedVariants([]);
	    self.loading(true);
	    var input = new Input('Input' + new Date().getTime(), 2, self.variants);
	    var data = ko.toJSON(input);
	    console.log(data);

	    var iid = null;

	    $.ajax({'type':'POST',
		    'url': 'http://localhost:8080/mutadelic/inputs',
		    'contentType': 'application/json',
		    'data':data,
		    'dataType': 'json',
		    'success':function(returnedData){
			console.log("iid=" + returnedData);
			iid = returnedData;

			var oid = null;

			$.ajax({'type': 'POST',
				'url': 'http://localhost:8080/mutadelic/outputs?input_id=' + iid,
				'contentType': 'application/json',
				'data':null,
				'dataType': 'json',
				'success': function(returnedData) {
				    console.log("oid=" + returnedData);
				    oid = returnedData;

				    $.ajax({'url': 'http://localhost:8080/mutadelic/outputs/' + oid,
					    'contentType': 'application/json',
					    'dataType': 'json',
					    'success': function(returnedData) {
						console.log(JSON.stringify(returnedData));
						console.log(returnedData.results);
						self.annotatedVariants(returnedData.results);
						console.log(self.annotatedVariants());
						$("#submitButton").button('reset');
						self.loading(false);
					    }});
				}});
		    }});
	}
    }

    self.chroms = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', 
		  '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y'];

    self.selectedOutputGroup = ko.observable("all-var-list-group-item");

    self.annotatedVariants = ko.observableArray();

    self.variants = ko.observableArray();

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
	self._chromosome('');
	self._strand('+');
	self._start('');
	self._end('');
	self._reference('');
	self._observed('');
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

$.ajaxPrefilter( "json script", function( options ) {
  options.crossDomain = true;
});

$(document).ready(function(){
    $('.collapse').collapse({toggle: false});
    setUpInputMasks();
});
