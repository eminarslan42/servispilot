// Check for error messages in URL and display them
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach(function(tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Set up brand selection dropdown event listeners
    document.querySelectorAll('.brand-select').forEach(function(select) {
        select.addEventListener('change', function() {
            const brandValue = this.value;
            const vehicleId = this.id.replace('brandSelect', '');
            const modelSelect = document.getElementById('modelSelect' + vehicleId);

            if (brandValue) {
                // Clear the model dropdown
                while (modelSelect.options.length > 1) {
                    modelSelect.remove(1);
                }

                // Fetch models for the selected brand
                fetch('/api/brands/' + encodeURIComponent(brandValue) + '/models')
                    .then(response => response.json())
                    .then(models => {
                        models.forEach(model => {
                            const option = document.createElement('option');
                            option.value = model;
                            option.textContent = model;
                            modelSelect.appendChild(option);
                        });
                    })
                    .catch(error => {
                        console.error('Error fetching models:', error);
                    });
            } else {
                // Clear the model dropdown if no brand is selected
                while (modelSelect.options.length > 1) {
                    modelSelect.remove(1);
                }
            }
        });
    });
});

/**
 * AJAX form submission to update a vehicle
 * @param {Event} event - The submit event
 * @param {HTMLFormElement} form - The form element
 */
function updateVehicle(event, form) {
    event.preventDefault();
    
    // Get the vehicle ID from the form
    const vehicleId = form.querySelector('input[name="id"]').value;
    
    // Get form data and create a FormData object
    const formData = new FormData(form);
    const data = {};
    
    // Convert FormData to JSON object
    formData.forEach((value, key) => {
        data[key] = value;
    });
    
    // Disable the save button and show loading indicator
    const saveButton = document.getElementById('saveButton' + vehicleId);
    const originalButtonHtml = saveButton.innerHTML;
    saveButton.disabled = true;
    saveButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Kaydediliyor...';
    
    // Hide any previous alerts
    const successAlert = document.getElementById('successAlert' + vehicleId);
    const errorAlert = document.getElementById('errorAlert' + vehicleId);
    successAlert.classList.add('d-none');
    errorAlert.classList.add('d-none');
    
    // Send AJAX request
    fetch('/vehicles/' + vehicleId + '/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Güvenli CSRF token yönetimi
            ...(document.querySelector('meta[name="_csrf_header"]') && {
                [document.querySelector('meta[name="_csrf_header"]').getAttribute('content')]: 
                document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
            })
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Unknown error occurred'); });
        }
        return response.json();
    })
    .then(result => {
        // Show success message
        successAlert.classList.remove('d-none');
        
        // Reset the save button
        saveButton.disabled = false;
        saveButton.innerHTML = originalButtonHtml;
        
        // Reload the page after a short delay to show the updates
        setTimeout(() => {
            window.location.reload();
        }, 1500);
    })
    .catch(error => {
        // Show error message
        const errorMessageElement = errorAlert.querySelector('.error-message');
        errorMessageElement.textContent = error.message || 'Bir hata oluştu. Lütfen tekrar deneyin.';
        errorAlert.classList.remove('d-none');
        
        // Reset the save button
        saveButton.disabled = false;
        saveButton.innerHTML = originalButtonHtml;
        
        console.error('Update error:', error);
    });
} 