$(document.body).ready(
		function() {
			$(".detailWrap").each(function() {
				$(this).css("margin", $.browser.msie ? "5px 0px 15px 0px" : "5px 8px 15px 8px");
			})
			$(".pdaTitle").each(
					function() {
						var self = this;
						$(this).bind(
								"mouseover",
								function(event) {
									$(self).css(
											"backgroundImage",
											"url(/yunmei/home/imgs/" + self.id
													+ "HighLight.gif)");
								})
						$(this).bind(
								"mouseout",
								function() {
									$(self).css(
											"backgroundImage",
											"url(/yunmei/home/imgs/" + self.id
													+ "Unlight.gif)");
								})
					})
			$(".detail").find(".productTitle").each(function() {
				var self = this;
				$(this).bind("mouseover", function(event) {
					$(self).fadeOut("fast", function() {
						$(self).fadeIn("fast");
					})
				})
			})
			$(".item").find("a").each(function() {
				var self = this;
				$(this).bind("mouseover", function(event) {
					$(self).css("fontWeight", "bold");
				})
				$(this).bind("mouseout", function() {
					$(self).css("fontWeight", "normal");
				})
			})
			top.$("#frame_container").css("height", $(document).height());
		})
