let durationChart, top5Chart, successFailureChart, volumeChart;
let currentPage = 0;
let totalPages = 1;
let sortField = 'startTime';
let sortOrder = 'desc';

function getParams() {
    const machine = document.getElementById('machineSelect').value;
    const job = document.getElementById('jobSelect').value;
    let start = document.getElementById('startDate').value;
    let end = document.getElementById('endDate').value;
    const params = new URLSearchParams();

    if (machine) params.append('machine', machine);
    if (job) params.append('job', job);
    if (start) {
        if (start.length === 10) start += 'T00:00:00';
        else if (start.length === 16) start += ':00';
        params.append('start', start);
    }
    if (end) {
        if (end.length === 10) end += 'T23:59:59';
        else if (end.length === 16) end += ':59';
        params.append('end', end);
    }
    return params;
}

async function loadJobs() {
    const params = getParams();
    const jobParams = new URLSearchParams(params);
    jobParams.delete('job');
    try {
        const jobs = await fetch('/api/dashboard/jobs?' + jobParams).then(r => r.json());
        const select = document.getElementById('jobSelect');
        const currentValue = select.value;
        select.innerHTML = '<option value="">Tous les jobs</option>';
        jobs.forEach(job => {
            const opt = document.createElement('option');
            opt.value = job;
            opt.textContent = job;
            select.appendChild(opt);
        });
        if (currentValue) select.value = currentValue;
    } catch (err) {
        console.error('Erreur chargement jobs', err);
    }
}

function displayNoDataMessage(canvasId, message = 'Aucune donnée') {
    const canvas = document.getElementById(canvasId);
    if (canvas) {
        canvas.style.display = 'none';
        let msgDiv = document.getElementById(canvasId + '_msg');
        if (!msgDiv) {
            msgDiv = document.createElement('div');
            msgDiv.id = canvasId + '_msg';
            msgDiv.className = 'text-center text-muted my-5';
            canvas.parentNode.insertBefore(msgDiv, canvas.nextSibling);
        }
        msgDiv.innerText = message;
        msgDiv.style.display = 'block';
    }
}

function showChart(canvasId) {
    const canvas = document.getElementById(canvasId);
    if (canvas) {
        canvas.style.display = 'block';
        const msgDiv = document.getElementById(canvasId + '_msg');
        if (msgDiv) msgDiv.style.display = 'none';
    }
}

async function loadData(page = 0) {
    currentPage = page;
    const params = getParams();
    params.append('page', page);
    params.append('size', 10);
    params.append('sortField', sortField);
    params.append('sortOrder', sortOrder);

    try {
        const kpis = await fetch('/api/dashboard/kpis?' + getParams()).then(r => r.json());
        document.getElementById('kpiTotal').innerText = kpis.totalExecutions;
        document.getElementById('kpiSuccess').innerText = kpis.successRate + '%';
        document.getElementById('kpiDuration').innerText = Math.round(kpis.avgDurationMs) + ' ms';
        document.getElementById('kpiErrors').innerText = kpis.totalErrors;
        document.getElementById('kpiRows').innerText = kpis.totalRows;

        const base = '/api/dashboard/';

        const durations = await fetch(base + 'duration-timeseries?' + getParams()).then(r => r.json());
        if (durations.length === 0) {
            if (durationChart) { durationChart.destroy(); durationChart = null; }
            displayNoDataMessage('durationChart', 'Aucune exécution');
        } else {
            showChart('durationChart');
            const durLabels = durations.map(d => d.start);
            const durData = durations.map(d => d.duration);
            if (durationChart) durationChart.destroy();
            durationChart = new Chart(document.getElementById('durationChart'), {
                type: 'line',
                data: { labels: durLabels, datasets: [{ label: 'Durée (ms)', data: durData }] }
            });
        }

        const top5 = await fetch(base + 'top5-longest?' + getParams()).then(r => r.json());
        if (top5.length === 0) {
            if (top5Chart) { top5Chart.destroy(); top5Chart = null; }
            displayNoDataMessage('top5Chart', 'Aucun job');
        } else {
            showChart('top5Chart');
            if (top5Chart) top5Chart.destroy();
            top5Chart = new Chart(document.getElementById('top5Chart'), {
                type: 'bar',
                data: { labels: top5.map(t => t.jobName), datasets: [{ label: 'Durée (ms)', data: top5.map(t => t.durationMs) }] },
                options: { indexAxis: 'y' }
            });
        }

        const sf = await fetch(base + 'success-failure-job?' + getParams()).then(r => r.json());
        if (sf.length === 0) {
            if (successFailureChart) { successFailureChart.destroy(); successFailureChart = null; }
            displayNoDataMessage('successFailureChart', 'Aucune exécution');
        } else {
            showChart('successFailureChart');
            if (successFailureChart) successFailureChart.destroy();
            successFailureChart = new Chart(document.getElementById('successFailureChart'), {
                type: 'bar',
                data: {
                    labels: sf.map(s => s.jobName),
                    datasets: [
                        { label: 'Succès', data: sf.map(s => s.success), backgroundColor: 'green' },
                        { label: 'Échec', data: sf.map(s => s.failure), backgroundColor: 'red' }
                    ]
                }
            });
        }

        const vol = await fetch(base + 'volume-by-job?' + getParams()).then(r => r.json());
        if (vol.length === 0) {
            if (volumeChart) { volumeChart.destroy(); volumeChart = null; }
            displayNoDataMessage('volumeChart', 'Aucune donnée de volume');
        } else {
            showChart('volumeChart');
            if (volumeChart) volumeChart.destroy();
            volumeChart = new Chart(document.getElementById('volumeChart'), {
                type: 'bar',
                data: { labels: vol.map(v => v.jobName), datasets: [{ label: 'Lignes', data: vol.map(v => v.totalRows) }] }
            });
        }

        const execsResp = await fetch('/api/executions?' + params).then(r => r.json());
        const execs = execsResp.content;
        totalPages = execsResp.totalPages;
        currentPage = execsResp.currentPage;

        const tbody = document.querySelector('#executionsTable tbody');
        tbody.innerHTML = '';
        if (execs.length === 0) {
            const row = tbody.insertRow();
            const cell = row.insertCell();
            cell.colSpan = 5;
            cell.className = 'text-center text-muted';
            cell.innerText = 'Aucune exécution trouvée';
        } else {
            execs.forEach(je => {
                const row = tbody.insertRow();
                row.insertCell().innerText = je.jobName;
                row.insertCell().innerText = je.startTime;
                row.insertCell().innerText = je.durationMs;
                row.insertCell().innerText = je.status;
                const btn = document.createElement('button');
                btn.className = 'btn btn-sm btn-info';
                btn.innerText = 'Détails';
                btn.onclick = () => showDetails(je.executionId, je.status);
                row.insertCell().appendChild(btn);
            });
        }

        document.getElementById('prevBtn').disabled = (currentPage === 0);
        document.getElementById('nextBtn').disabled = (currentPage >= totalPages - 1);
        document.getElementById('pageInfo').innerText = totalPages > 0 ? `Page ${currentPage + 1} / ${totalPages}` : 'Aucune page';

    } catch (error) {
        console.error('Erreur chargement données', error);
    }
}

function showDetails(executionId, status) {
    const errorTbody = document.getElementById('errorTable').querySelector('tbody');
    const meterTbody = document.getElementById('meterTable').querySelector('tbody');
    errorTbody.innerHTML = '';
    meterTbody.innerHTML = '';

    fetch(`/api/executions/by-exec-id/${executionId}/errors`)
        .then(r => r.json())
        .then(errors => {
            if (errors.length === 0) {
                const row = errorTbody.insertRow();
                const cell = row.insertCell();
                cell.colSpan = 3;
                cell.className = 'text-center text-muted';
                if (status && status.toUpperCase() === 'FAILURE') {
                    cell.innerText = 'Job terminé en échec (aucune erreur détaillée)';
                } else {
                    cell.innerText = 'Aucune erreur';
                }
            } else {
                errors.forEach(e => {
                    const row = errorTbody.insertRow();
                    row.insertCell().innerText = e.timestamp;
                    row.insertCell().innerText = e.component;
                    row.insertCell().innerText = e.exceptionMessage;
                });
            }
        })
        .catch(err => console.error('Erreur chargement erreurs', err));

    fetch(`/api/executions/by-exec-id/${executionId}/meters`)
        .then(r => r.json())
        .then(meters => {
            if (meters.length === 0) {
                const row = meterTbody.insertRow();
                const cell = row.insertCell();
                cell.colSpan = 6;  // 6 colonnes
                cell.className = 'text-center text-muted';
                cell.innerText = 'Aucune métrique';
            } else {
                meters.forEach(m => {
                    const row = meterTbody.insertRow();
                    row.insertCell().innerText = m.meterName;
                    row.insertCell().innerText = m.counterName;
                    row.insertCell().innerText = m.value;
                    const refVal = m.reference || 0;
                    row.insertCell().innerText = refVal;

                    // Pourcentage
                    let percent = 0;
                    if (refVal > 0) {
                        percent = (m.value / refVal) * 100;
                    }
                    const percentCell = row.insertCell();
                    percentCell.innerText = percent.toFixed(1) + '%';

                    // Seuils et couleur
                    let thresholdInfo = parseThresholds(m.thresholds, percent);
                    const stateCell = row.insertCell();
                    if (thresholdInfo) {
                        stateCell.innerHTML = `<span style="background-color:${thresholdInfo.color}; padding:2px 6px; border-radius:4px; color:white;">${thresholdInfo.label}</span>`;
                        row.style.backgroundColor = thresholdInfo.color + '20';
                    } else {
                        stateCell.innerText = '-';
                    }
                });
            }
        })
        .catch(err => console.error('Erreur chargement meters', err));

    new bootstrap.Modal(document.getElementById('detailModal')).show();
}

/**
 * Analyse les seuils au format "label|min|max|R|G|B#..."
 * Retourne l'objet {label, color, min, max} correspondant au pourcentage,
 * ou null si aucun seuil ne correspond.
 */
function parseThresholds(thresholdsStr, percent) {
    if (!thresholdsStr) return null;
    const segments = thresholdsStr.split('#');
    for (let seg of segments) {
        const parts = seg.split('|');
        if (parts.length >= 6) {
            const label = parts[0];
            const min = parseFloat(parts[1]);
            const max = parseFloat(parts[2]);
            const R = parts[3];
            const G = parts[4];
            const B = parts[5];
            if (!isNaN(min) && !isNaN(max) && percent >= min && percent <= max) {
                return {
                    label: label,
                    color: `rgb(${R},${G},${B})`,
                    min: min,
                    max: max
                };
            }
        }
    }
    return null;
}

function resetFilters() {
    document.getElementById('machineSelect').value = '';
    document.getElementById('jobSelect').value = '';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    loadJobs().then(() => loadData(0));
}

// Tri sur les entêtes
document.querySelectorAll('#executionsTable th[data-sort]').forEach(th => {
    th.addEventListener('click', function() {
        const field = this.dataset.sort;
        if (sortField === field) {
            sortOrder = sortOrder === 'asc' ? 'desc' : 'asc';
        } else {
            sortField = field;
            sortOrder = 'asc';
        }
        loadData(0);
    });
});

// Pagination
document.getElementById('prevBtn').addEventListener('click', () => {
    if (currentPage > 0) loadData(currentPage - 1);
});
document.getElementById('nextBtn').addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadData(currentPage + 1);
});

// Rafraîchir (manuel + auto)
document.getElementById('refreshBtn').addEventListener('click', () => {
    loadData(0);
    startAutoRefresh(); // relance le minuteur après un rafraîchissement manuel
});

document.getElementById('resetBtn').addEventListener('click', resetFilters);
document.getElementById('reloadJobsBtn')?.addEventListener('click', () => loadJobs());

// Auto-refresh toutes les 60 secondes
let autoRefreshInterval;
function startAutoRefresh() {
    if (autoRefreshInterval) clearInterval(autoRefreshInterval);
    autoRefreshInterval = setInterval(() => {
        loadData(currentPage);
    }, 60000);
}

window.addEventListener('load', () => {
    showChart('durationChart');
    showChart('top5Chart');
    showChart('successFailureChart');
    showChart('volumeChart');
    loadJobs().then(() => {
        loadData(0);
        startAutoRefresh();
    });
});