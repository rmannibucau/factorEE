import DataProtectionLaw from 'service/cookie.service';

let lazyLoad = () => {
    if (!window.ga) {
        return;
    }
    ((i,s,o,g,r,a,m) => {i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
            (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    window.ga('create', window.JavaEEFactory.analytics, 'auto');
};
let send = p => {
    window.ga('set', 'page', p);
    window.ga('send', 'pageview');
};

export default {
    active() {
        return !!window.JavaEEFactory.analytics;
    },
    pageView(path) {
        if (!this.active()) {
            return;
        }

        DataProtectionLaw.onAccepted(() => {
            lazyLoad();
            send(path);
        });
    }
};
