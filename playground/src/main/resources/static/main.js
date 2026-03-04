function toggleCli(header) {
    var section = header.closest('.cli-command-section');
    var code = section.querySelector('.cli-command-code');
    var arrow = header.querySelector('span');
    var open = code.style.display !== 'none';
    code.style.display = open ? 'none' : 'block';
    arrow.textContent = open ? '▶' : '▼';
}

function copyCli(btn) {
    if (!navigator.clipboard) {
        console.warn('Clipboard API not available — use localhost instead of 0.0.0.0');
        return;
    }
    var code = btn.closest('.cli-command-section').querySelector('.cli-command-code').textContent;
    navigator.clipboard.writeText(code).then(function() {
        btn.textContent = 'Copied!';
        setTimeout(function() { btn.textContent = 'Copy'; }, 1500);
    });
}
