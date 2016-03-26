import VueRouter from "VueRouter";
import AnalyticsService from "service/analytics.service";
import LayoutController from "controller/layout.controller";
import FactoryController from "controller/factory/generator.controller";

var router = new VueRouter();
router.map({
    '/': {
        component: LayoutController,
        subRoutes: { // in case we have multiple pages
            '/': {component: FactoryController}
        }
    }
});
router.redirect({'*': '/'});
router.beforeEach(transition => {
    transition.next();

    var path = transition.to.path;
    if (AnalyticsService.active()) {
        AnalyticsService.pageView(path);
    }
});

export default router;
