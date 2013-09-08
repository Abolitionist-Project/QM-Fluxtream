$(document).ready(function () {
	var hasTOC = $('rest-method-toc').length !== 0;
	var toc = {};
	
	var displayDocumentation = function(replacement, node) {
		//if (!('nodeName' in node)) { return; }
		var item = $(node);
		switch (node.nodeName.toLowerCase()) {
			case 'rest-method':
				replacement.append($('<hr/>'));
				
				var httpMethod = item.attr('method');
				var url = item.attr('url');
				
				// If there's a table of contents, add a uniquely-named anchor
				if (hasTOC) {
					var tocAnchor = httpMethod + '-' + url;
					if (tocAnchor in toc) {
						var i = 0;
						while ((tocAnchor + '-' + i) in toc) { i++; }
						tocAnchor = tocAnchor + '-' + i;
					}
					toc[tocAnchor] = httpMethod + ' ' + url;
					replacement.append($('<a/>').attr('name', tocAnchor));
				}
				// Add the HTTP method and URL
				replacement.append($('<h2/>').text(httpMethod + ' ' + url));
				item.children().each(function() { displayDocumentation(replacement, this); });
				replacement.append($('<a/>').attr('href', '#toc').text('Back to top'));
				break;
				
			case 'parameter-list':
				replacement.append($('<h4/>').text('Query parameters'));
				var list = $('<dl/>');
				item.children().each(function() { displayDocumentation(list, this); });
				replacement.append(list);
				break;
				
			case 'parameter':
				var example = item.attr('example');
				var description = item.text();
				
				replacement.append($('<dt/>').text(item.attr('name')));
				var text = item.text() || '[No description]';
				var example = item.attr('example');
				replacement.append($('<dd/>').text(text + (example !== undefined ? ' (example: ' + item.attr('example') + ')' : '')));
				break;
				
			case 'request-body':
				replacement.append($('<h4/>').text('Request body'));
				var list = $('<ul/>');
				item.children().each(function() { displayDocumentation(list, this); });
				replacement.append(list);
				break;
				
			case 'json-array':
				replacement.append($('<li/>').text('JSON array of the following:'));
				var list = $('<ul/>');
				item.children().each(function() { displayDocumentation(list, this); });
				replacement.append(list);
				break;
				
			case 'json-object':
				var table = $('<table/>').attr('border', '1');
				var headerRow = $('<tr/>');
				headerRow.append($('<th/>').text('Parameter name'));
				headerRow.append($('<th/>').text('Type'));
				headerRow.append($('<th/>').text('Permitted values'));
				headerRow.append($('<th/>').text('Description'));
				table.append(headerRow);
				item.children().each(function() { displayDocumentation(table, this); });
				replacement.append(table);
				break;
				
			case 'json-parameter':
				var tableRow = $('<tr/>');
				tableRow.append($('<td/>').text(item.attr('name')));
				var type = item.attr('type') || 'string';
				if (type == 'array') {
					var tableCell = $('<td/>').attr('colspan', 3);
					item.children().each(function() { displayDocumentation(tableCell, this); });
					tableRow.append(tableCell);
				} else {
					tableRow.append($('<td/>').text(item.attr('type') || 'string'));
					var values = item.attr('values');
					if (values) {
						tableRow.append($('<td/>').text(values.split(',').map(function(item) { return item.trim(); }).join(', ')));
					} else {
						tableRow.append($('<td/>'));
					}
					tableRow.append($('<td/>').text(item.text()));
				}
				replacement.append(tableRow);
				break;
		}
		
	}
	
	// Replace the REST method summary with HTML
	$('rest-method').each(function () {
		var replacement = $('<div/>').addClass('rest-method');
		displayDocumentation(replacement, this);
		$(this).replaceWith(replacement);
	});
	
	// Replace the table-of-contents placeholder with HTML
	if (hasTOC) {
		$('rest-method-toc').each(function() {
			var replacement = $('<div/>').addClass('rest-method-toc');
			
			// Add back-to-top anchor
			replacement.append($('<a/>').attr('name', 'toc'));
			// Add table-of-contents section title
			replacement.append($('<h2/>').text('Table of contents'));
			// Add list of contents
			var tocList = $('<ul/>');
			for (link in toc) {
				tocList.append($('<li/>').append($('<a/>').attr('href', '#' + link).text(toc[link])));
			}
			replacement.append(tocList);
			
			$(this).replaceWith(replacement);
		});
	}
});
