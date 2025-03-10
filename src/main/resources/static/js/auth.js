// JWT token'ı kontrol et ve yönlendirme yap
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login';
        return;
    }
    return token;
}

// Sayfa yüklendiğinde auth kontrolü yap
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
});

// Tüm fetch isteklerine token ekle
const originalFetch = window.fetch;
window.fetch = function() {
    const token = localStorage.getItem('token');
    if (token) {
        if (arguments[1] === undefined) {
            arguments[1] = {};
        }
        if (arguments[1].headers === undefined) {
            arguments[1].headers = {};
        }
        arguments[1].headers['Authorization'] = 'Bearer ' + token;
    }
    return originalFetch.apply(this, arguments);
}; 