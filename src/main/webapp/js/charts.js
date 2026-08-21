let jolChart;

function loadJolChart(data) {
    const months = Object.keys(data);
    const stats = Object.values(data);

    if (jolChart) {
        jolChart.destroy();
    }

    jolChart = new Chart(document.getElementById('jolChart'), {
        type: 'line',
        data: {
            labels: months,
            datasets: [
                {
                    label: 'Games Started',
                    data: stats.map(stat => stat.gamesStartedPerMonth),
                    tension: 0.3
                },
                {
                    label: 'Games Ended',
                    data: stats.map(stat => stat.gamesEndedPerMonth),
                    tension: 0.3
                },
                {
                    label: 'Wins',
                    data: stats.map(stat => stat.winsPerMonth),
                    tension: 0.3
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

let personalChart;

function loadPersonalChart(data) {
    const top10 = Object.values(data)
        .sort((a, b) => b.games - a.games)
        .slice(0, 10);

    if (personalChart) {
        personalChart.destroy();
    }

    personalChart = new Chart(document.getElementById('personalChart'), {
        type: 'bar',

        data: {
            labels: top10.map(stat => stat.opponent),

            datasets: [
                {
                    label: 'Games',
                    data: top10.map(stat => stat.games)
                },
                {
                    label: 'You Won',
                    data: top10.map(stat => stat.wins)
                },
                {
                    label: 'Opponent Won',
                    data: top10.map(stat => stat.winOpponent)
                },
                {
                    label: 'Someone Other Won',
                    data: top10.map(stat => stat.winOther)
                }
            ]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,

            plugins: {
                legend: {
                    display: true
                },

                datalabels: {
                    color: '#000',
                    anchor: 'end',
                    align: 'top',
                    font: {
                        weight: 'bold',
                        size: 12
                    },
                    formatter: value => value
                }
            },

            scales: {
                x: {
                    ticks: {
                        autoSkip: false
                    }
                },

                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        },

        plugins: [ChartDataLabels]
    });
}

function createPieChart(canvasId, stats, valueKey, title, onlyTop) {
    const entries = Object.entries(stats)
        .map(([nation, data]) => ({
            nation,
            value: parseFloat(data[valueKey]) || 0
        }))
        .sort((a, b) => b.value - a.value);

    if (onlyTop) {
        entries.splice(10);
    }

    new Chart(document.getElementById(canvasId), {
        type: 'pie',

        data: {
            labels: entries.map(x => regionNames.of(x.nation)),
            datasets: [{
                data: entries.map(x => x.value)
            }]
        },

        plugins: [ChartDataLabels],

        options: {
            responsive: true,

            plugins: {
                legend: {
                    display: false
                },

                // Title above the pie
                title: {
                    display: true,
                    text: title,
                    font: {
                        size: 18,
                        weight: 'bold'
                    },
                    padding: {
                        bottom: 15
                    }
                },

                datalabels: {
                    color: '#fff',
                    formatter: function(value, context) {
                        return context.chart.data.labels[context.dataIndex];
                    },
                    font: {
                        weight: 'bold',
                        size: 11
                    }
                },

                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.label}: ${context.raw}`;
                        }
                    }
                }
            }
        }
    });
}