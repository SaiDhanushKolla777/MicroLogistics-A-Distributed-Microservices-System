document.addEventListener("DOMContentLoaded", function() {
    fetch('/api/dashboard')
        .then(response => response.json())
        .then(data => {
            document.getElementById('dashboard-json').textContent = JSON.stringify(data, null, 2);
        });

    fetch('/api/dashboard/alerts')
        .then(response => response.json())
        .then(alerts => {
            let alertArea = document.getElementById('alerts');
            alertArea.innerHTML = '';
            alerts.forEach(alert => {
                let div = document.createElement('div');
                div.className = 'alert';
                div.innerText = `[${alert.level}] ${alert.timestamp}: ${alert.message}`;
                alertArea.appendChild(div);
            });
        });
});
