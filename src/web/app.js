let chart = null;
let compareChart = null;
async function loadData() {
    const country = document.getElementById('countrySelect').value;
    document.getElementById('status').textContent = 'Fetching data';
    
    try {
        const response = await fetch(
            `http://localhost:8080/energyapi/energy?country=${encodeURIComponent(country)}`
        );
        const data = await response.json();

        if (data.error) {
            document.getElementById('status').textContent =
                'Error: ' + data.error;
            return;
        }

        document.getElementById('status').textContent = 
            `Showing ${data.length} years of data for ${country}`;
        const labels = data.map(d => d.year ?? 0);
        const renewables = data.map(d => d.renewables);
        const fossil = data.map(d => d.fossil ?? 0);
        const nuclear = data.map(d => d.nuclear ?? 0);

        const last = data[data.length - 1];
        const first = data[0];
        document.getElementById('statRenewables').textContent = 
            (last.renewables ?? 0).toFixed(1);
        document.getElementById('statFossil').textContent = 
            (last.fossil ?? 0).toFixed(1);
        document.getElementById('statNuclear').textContent =
            (last.nuclear ?? 0).toFixed(1);
        const totalLatest = last.renewables +
                            last.fossil + last.nuclear;
        const nuclearShare = 
            ((last.nuclear / totalLatest) * 100).toFixed(2);
        document.getElementById('nuclearShare').textContent =
            `${nuclearShare}%`;

        if (chart) chart.destroy();
        const ctx =
            document.getElementById('energyChart').getContext('2d');
        chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels, // x-axis of years
                datasets: [
                    {
                        label: 'Renewables (TWh)',
                        data: renewables,
                        borderColor: '#22c55e',
                        backgroundColor: 'rgba(34,197,94,0.1)',
                        fill: true,
                        tension: 0.3,
                        pointRadius: 2
                    },
                    {
                        label: 'Fossil (TWh)',
                        data: fossil,
                        borderColor: '#f97316',
                        backgroundColor: 'rgba(249,115,22,0.1)',
                        fill: true,
                        tension: 0.3,
                        pointRadius: 2
                    },
                    {
                        label: 'Nuclear (TWh)',
                        data: nuclear,
                        borderColor: '#a78bfa',
                        backgroundColor: 'rgba(167,139,250,0.1)',
                        fill: true,
                        tension: 0.3,
                        pointRadius: 2
                    }
                ]
            },
            options: { 
                responsive: true,
                plugins: {
                    legend: { labels: {color: '#ccc'} },
                    title: {
                        display: true,
                        text: `Energy Mix -- ${country}`,
                        color: '#fff',
                        font: { size: 16 }
                    }
                },
                scales: {
                    x: { 
                        ticks: { color: '#888' },
                        grid: { color: '#2a2a3a' } 
                    },
                    y: {
                        ticks: { color: '#888' },
                        grid: { color: '#2a2a3a' },
                        title: { display: true, text: 'TWh', color:'#888' }
                    }
                }
            }
        });
    } catch (error) {
        document.getElementById('status').textContent =
            'Failed to fetch: ' + error.message;
    }
}
async function loadCompare() {
    const type = document.getElementById('typeSelect').value;
    const year = document.getElementById('yearInput').value;

    try {
        const response = await fetch(
            `http://localhost:8080/energyapi/compare?type=${type}&year=${year}`
        );
        const data = await response.json();
        const labels = data.map(d => d.country);
        const values = data.map(d => d.value);
        const colors = {
            nuclear:    'rgba(167,139,250,0.8)', // purple
            renewables: 'rgba(34,197,94,0.8)',   // green
            fossil:     'rgba(249,115,22,0.8)'   // orange
        };
        if (compareChart) compareChart.destroy();
        const ctx = document.getElementById('compareChart').getContext('2d');
        compareChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: `${type.charAt(0).toUpperCase() + 
                           type.slice(1)} Electricity ${year} (TWh)`,
                    data: values,
                    backgroundColor: colors[type],
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { labels: { color: '#ccc' } },
                    title: {
                        display: true,
                        text: `Country ${type.charAt(0).toUpperCase() +
                                type.slice(1)} Comparison -- ${year}`,
                        color: '#fff',
                        font: { size: 16 }
                    }
                },
                scales: {
                    x: {
                        ticks: { color: '#888' },
                        grid: { display: false }
                    },
                    y: {
                        ticks: { color: '#888' },
                        grid: { color: '#2a2a3a' },
                        title: { 
                            display: true, 
                            text: 'TWh', 
                            color: '#888' 
                        }
                    }
                }
            }
        });
    } catch(error) {
        document.getElementById('compareStatus').textContent =
        'Compare failed: ' + error.message;
    }
}   
// Load the default (US) so they see graph right away
loadData();
loadCompare();
