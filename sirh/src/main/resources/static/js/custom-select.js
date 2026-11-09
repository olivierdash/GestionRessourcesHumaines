(function() {
    function initCustomSelect(wrapper) {
        const searchInput = wrapper.querySelector('.custom-select-search');
        const dropdown = wrapper.querySelector('.custom-dropdown');
        const hiddenSelect = wrapper.querySelector('.custom-select-hidden');

        if (!searchInput || !dropdown || !hiddenSelect) return;

        // 1. Initialiser les options du dropdown à partir du select caché
        const options = Array.from(hiddenSelect.options).filter(opt => opt.value !== "");
        
        dropdown.innerHTML = ''; // Nettoyer au cas où
        
        options.forEach(opt => {
            const div = document.createElement('div');
            div.className = 'custom-option';
            div.textContent = opt.text;
            div.dataset.value = opt.value;
            
            // Si l'option est déjà sélectionnée (utile pour la page d'édition)
            if (opt.selected) {
                div.classList.add('active');
                searchInput.value = opt.text;
            }

            div.addEventListener('click', () => {
                searchInput.value = opt.text;
                hiddenSelect.value = opt.value;
                // Déclencher l'événement change pour que d'autres scripts puissent réagir
                hiddenSelect.dispatchEvent(new Event('change', { bubbles: true }));
                
                // Fermer le dropdown
                dropdown.classList.remove('show');
                
                // Retirer l'état actif de tous et l'ajouter à celui-ci
                dropdown.querySelectorAll('.custom-option').forEach(el => el.classList.remove('active'));
                div.classList.add('active');
            });

            dropdown.appendChild(div);
        });

        // 2. Ouvrir au focus
        searchInput.addEventListener('focus', () => {
            dropdown.classList.add('show');
            filterList();
        });

        // 3. Filtrer à la saisie
        searchInput.addEventListener('input', () => {
            hiddenSelect.value = ""; // Réinitialiser si l'utilisateur tape à nouveau
            filterList();
        });

        // 4. Fermer en cliquant ailleurs
        document.addEventListener('click', (e) => {
            if (!wrapper.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        // Fonction de filtrage interne
        function filterList() {
            const query = searchInput.value.toLowerCase();
            const items = dropdown.querySelectorAll('.custom-option:not(.no-result)');
            let hasVisible = false;

            items.forEach(item => {
                if (item.textContent.toLowerCase().includes(query)) {
                    item.style.display = 'block';
                    hasVisible = true;
                } else {
                    item.style.display = 'none';
                }
            });

            // Gérer le message "Aucun résultat"
            let noResultDiv = dropdown.querySelector('.no-result');
            if (!hasVisible) {
                if (!noResultDiv) {
                    noResultDiv = document.createElement('div');
                    noResultDiv.className = 'custom-option no-result';
                    noResultDiv.style.cursor = 'default';
                    noResultDiv.style.fontStyle = 'italic';
                    noResultDiv.style.color = '#64748b'; // Slate-500
                    noResultDiv.textContent = 'Aucun résultat trouvé';
                    dropdown.appendChild(noResultDiv);
                }
                noResultDiv.style.display = 'block';
            } else if (noResultDiv) {
                noResultDiv.style.display = 'none';
            }
        }
    }

    // Initialiser tous les composants au chargement de la page
    document.addEventListener('DOMContentLoaded', () => {
        const wrappers = document.querySelectorAll('.custom-select-wrapper');
        wrappers.forEach(initCustomSelect);
    });
})();