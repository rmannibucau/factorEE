import http from "util/http";
import Constants from "Constants";

export default {
    getConfiguration(onSuccess, onError) {
        http.get(Constants.root + '/api/factory')
            .then(response => onSuccess(response.data), onError);
    },
    generate(project, onSuccess, onError) { // needs binary format so don't use vue-resource
        var xhr = new XMLHttpRequest();
        xhr.responseType = "arraybuffer";
        xhr.open('POST', Constants.root + '/api/factory');
        xhr.setRequestHeader('Content-type', 'application/json');
        xhr.setRequestHeader('Accept', 'application/zip');
        xhr.send(JSON.stringify(project));
        xhr.onreadystatechange = oEvent => {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    onSuccess(xhr.response);
                } else {
                    onError();
                }
            }
        }
    }
};
