import Cookies from 'jquery-cookie';

let COOKIE_NAME = 'javaeefactory-data-low-protection';
let MESSAGE_ID = '#data-low-protection';

let DataProtectionLaw = class DataProtectionLaw {
    constructor() {
        this.callbacksOnAccept = [];
        this.accepted = Cookies.get(COOKIE_NAME) == 'accepted';
    }

    onAccepted(cb) {
        if (!this.accepted) {
            this.callbacksOnAccept.push(cb);
        } else {
            cb();
        }
    }

    onAccept() {
        this.callbacksOnAccept.forEach(c => c());
        Cookies.set(COOKIE_NAME, "accepted");
        this.accepted = true;
    }

    onReject() { // don't set a cookie hoping user will change his mind next time
        this.accepted = false;
        this.callbacksOnAccept = [];
    }

    reset() {
        Cookies.remove(COOKIE_NAME);
    }
}


export default new DataProtectionLaw();
