import Constants from "Constants";
import Vue from "Vue";
import template from "text!template/layout.html";
import errorHandler from "util/notification";
import DataProtectionLaw from 'service/cookie.service';

export default Vue.extend({
    template,
    data() {
        return {
            showDataProtectionLawMessage: !DataProtectionLaw.accepted
        };
    },
    methods: {
        acceptCookies() {
            DataProtectionLaw.onAccept();
            this.showDataProtectionLawMessage = false;
        },
        rejectCookies() {
            DataProtectionLaw.onReject();
            this.showDataProtectionLawMessage = false;
        }
    }
});
