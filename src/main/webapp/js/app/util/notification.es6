import notificationTemplate from "text!template/notification.html";
import "boostrapNotify";

export default {
    notify: (type, title, content) => {
        var usedType = type || 'info';
        $.notify(
            {title: '<strong>' + (title || usedType) + '</strong>', message: content},
            {type: usedType, newest_on_top: true, template: notificationTemplate});
    },
    error(title, message) {
        this.notify('danger', title, message);
    },
    success(title, message) {
        this.notify('success', title, message);
    },
    info(title, message) {
        this.notify('info', title, message);
    },
    warning(title, message) {
        this.notify('warning', title, message);
    }
};
