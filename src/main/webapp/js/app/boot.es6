import router from "mapping";
import Vue from "Vue";
import $ from "jquery";
import Constants from "Constants";

var autoResize = () => {
    var height = ($(window).height() - (Constants.footerSize || 156)) + 'px';
    $('#main').css('min-height', height);
};
$(window).resize(autoResize);

router.start(Vue.extend({template: '<router-view></router-view>'}), '#content', autoResize);
