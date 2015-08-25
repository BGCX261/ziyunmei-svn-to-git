var frame;
$(document.body).ready( function() {
	var tabIds = $("#menu a");
	frame = $("#frame_container");
	$(".header_logo").bind("click", function() {
		frame.attr("src", "/yunmei/home/home.html");
	})
	tabIds.each( function() {
		var self = this;
		$(this).bind("click", function(event) {
			var self = this;
			frame.fadeOut("normal", function() {
				frame.attr("src", "/yunmei/home/" + self.id + ".html");
				frame.fadeIn("normal");
			});
			return false;
		});
	})
	frame.attr("src", "/yunmei/home/home.html");
})
