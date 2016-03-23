import DataProtectionLaw from 'service/cookie.service';

let getGA = () => {
    if (!window.ga) {
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');
        ga('create', window.JavaEEFactory.analytics, 'auto');
    }
    return window.ga;
};

let send = (ga, p) => {
    ga('set', 'page', p);
    ga('send', 'pageview');
};

export default {
    active() {
        return !!window.JavaEEFactory.analytics;
    },
    pageView(path) {
        if (!this.active()) {
            return;
        }
        DataProtectionLaw.onAccepted(() => send(getGA(), path));
    }
};
