// Example chart.js usage
document.addEventListener("DOMContentLoaded", function() {
    if (window.Chart && document.getElementById('chart')) {
        // Example static data
        const ctx = document.getElementById('chart').getContext('2d');
        const myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Throughput',
                    data: [120, 150, 180, 140, 200, 170, 210],
                    backgroundColor: 'rgba(15, 76, 129, 0.2)',
                    borderColor: '#0f4c81',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
});
