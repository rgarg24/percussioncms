/* 
 * jQuery - Collapser - Plugin v1.0
 * http://www.aakashweb.com/
 * Copyright 2010, Aakash Chakravarthy
 * Released under the MIT License.
 *
 * ERS (8/25/2010) - The functions showElement and hideElement have been modified to fix a bug
 * where the after collapse callback function was returning prematurely. I have submitted the fix
 * to the plugin's author in hopes that the fix will be integrated into the plugin.
 */

(function($){
    $.fn.collapser= function(options, beforeCallback, afterCallback) {
        
        var defaults = {
            target: 'next',
			targetOnly: null,
            effect: 'slide',
			changeText: true,
			expandHtml: 'Expand',
			collapseHtml: 'Collapse',
			expandClass: '',
			collapseClass:''
        };
        
        var options = $.extend(defaults, options);
		
		var expHtml,collHtml, effectShow, effectHide;
		
		if(options.effect == 'slide'){
			effectShow = 'slideDown';
			effectHide = 'slideUp';
		}else{
			effectShow = 'fadeIn';
			effectHide = 'fadeOut';
		}
		
		if(options.changeText == true){
			expHtml = options.expandHtml;
			collHtml = options.collapseHtml;
		}
		
		function callBeforeCallback(obj){
			if(beforeCallback !== undefined){
				beforeCallback.apply(obj);
			}
		}
		
		function callAfterCallback(obj){
			if(afterCallback !== undefined){
				afterCallback.apply(obj);
			}
		}
		
		function hideElement(obj, method){
			callBeforeCallback(obj);
			if(method == 1){
				obj[options.target](options.targetOnly)[effectHide](
            function(){
               obj.html(expHtml);
				   obj.removeClass(options.collapseClass);
				   obj.addClass(options.expandClass);
               callAfterCallback(obj);
            });
				
			}else{
				$(document).find(options.target)[effectHide](
            function(){
               obj.html(expHtml);
				   obj.removeClass(options.collapseClass);
				   obj.addClass(options.expandClass);
               callAfterCallback(obj);
            });
				
			}
			
		}
		
		function showElement(obj, method){
			callBeforeCallback(obj)
			if(method == 1){
				obj[options.target](options.targetOnly)[effectShow](
            function(){
               obj.html(collHtml);
				   obj.removeClass(options.expandClass);
				   obj.addClass(options.collapseClass);
               callAfterCallback(obj);
            });
				
			}else{
				$(document).find(options.target)[effectShow](
            function(){
               obj.html(collHtml);
				   obj.removeClass(options.expandClass);
				   obj.addClass(options.collapseClass); 
               callAfterCallback(obj);
            });
				
			}
			
		}
		
		function toggleElement(obj, method){
			if(method == 1){
				if(obj[options.target](options.targetOnly).is(':visible')){
					hideElement(obj, 1);
				}else{
					showElement(obj, 1);
				}
			}else{
				if($(document).find(options.target).is(':visible')){
					hideElement(obj, 2);
				}else{
					showElement(obj, 2);
				}
			}
		}
		
		return this.each(function(){
		   
		   if($.fn[options.target] && $(this)[options.target]()){
				$(this).toggle(function(){		
					toggleElement($(this), 1);
				},function(){
					toggleElement($(this), 1);
				});	
				
		   }else{
			   
			   $(this).toggle(function(){
					toggleElement($(this), 2);
				},function(){
					toggleElement($(this), 2);
				});
		   }
		   
		   // Initialize  
		   if($.fn[options.target] && $(this)[options.target]()){
				if($(this)[options.target]().is(':hidden')){
					$(this).html(expHtml);
					$(this).removeClass(options.collapseClass);
					$(this).addClass(options.expandClass);
				}else{
					$(this).html(collHtml);
					$(this).removeClass(options.expandClass);
					$(this).addClass(options.collapseClass);
				}
			}else{
				if($(document).find(options.target).is(':hidden')){
					$(this).html(expHtml);
				}else{
					$(this).html(collHtml);
				}
			}
		   
        });
    };
    
})(jQuery);