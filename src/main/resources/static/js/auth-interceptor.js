/**
 * AJAX isteklerine Authorization header'ı ekleyen interceptor
 */
$(document).ready(function() {
    // AJAX isteklerine X-Requested-With header'ı ekle
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
        }
    });

    // AJAX hata yakalama
    $(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError) {
        if (jqXHR.status === 401) {
            // Token yenilemeyi dene
            refreshToken().then(function(success) {
                if (success) {
                    // Token yenileme başarılıysa, önceki isteği tekrar et
                    $.ajax(ajaxSettings);
                } else {
                    // Token yenileme başarısızsa, login sayfasına yönlendir
                    alert("Oturumunuz sonlanmıştır. Lütfen tekrar giriş yapın.");
                    window.location.href = '/login';
                }
            });
        }
    });
    
    /**
     * Refresh token ile yeni bir access token alma
     * @returns {Promise<Boolean>} Token yenileme başarılı oldu mu?
     */
    function refreshToken() {
        return new Promise(function(resolve, reject) {
            $.ajax({
                url: '/api/auth/refresh-token',
                type: 'POST',
                contentType: 'application/json',
                dataType: 'json',
                xhrFields: {
                    withCredentials: true
                },
                success: function(response) {
                    console.log('Token başarıyla yenilendi');
                    resolve(true);
                },
                error: function() {
                    console.log('Token yenilenirken hata oluştu');
                    resolve(false);
                }
            });
        });
    }
}); 