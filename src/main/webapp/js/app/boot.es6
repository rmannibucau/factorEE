import router from "mapping";
import Vue from "Vue";
import $ from "jquery";
import Constants from "Constants";

router.start(Vue.extend({template: '<router-view></router-view>'}), '#content');

var autoResize = () => {
    var height = ($(window).height() - (Constants.footerSize || 170)) + 'px';
    $('#main').css('min-height', height);
};
autoResize();
$(window).resize(autoResize);
