document.addEventListener('DOMContentLoaded', () => {
    fetch('http://localhost:8080/project')
        .then(response => response.json())
        .then(data => {
                initProjectCard(data)
                initProjectName(data)
            }
        )
        .catch(error => console.error('Error:', error));
});

function initProjectName(projects) {
    const container = document.querySelector('.project-title');
    const title = document.createElement('h1');
    title.textContent = projects.name;
    container.appendChild(title);
}

function initProjectCard(project) {
    const container = document.querySelector('.card-container');
    console.log(
        project
    )
    for (const fileKey in project.files) {
        const name = project.files[fileKey].name
        console.log("name", name)

        const card = document.createElement('div');
        card.className = 'card';

        const img = document.createElement('img');
        img.src = `http://localhost:8080/preview/${project.id}/${fileKey}`; // Construct the image URL
        img.alt = 'SVG Image';

        const content = document.createElement('div');
        content.className = 'card-content';

        const title = document.createElement('h3');
        title.textContent = name; // Use the key as the title

        // Counter elements
        const counter = document.createElement('div');
        counter.className = 'counter';

        const decrementButton = document.createElement('button');
        decrementButton.textContent = '-';
        decrementButton.onclick = () => adjustCount(counterInput, -1);

        const counterInput = document.createElement('p');
        counterInput.type = 'number';
        counterInput.textContent = "1";
        counterInput.className = 'counter-input';

        const incrementButton = document.createElement('button');
        incrementButton.textContent = '+';
        incrementButton.onclick = () => adjustCount(counterInput, 1);

        counter.appendChild(decrementButton);
        counter.appendChild(counterInput);
        counter.appendChild(incrementButton);

        // Append all elements
        content.appendChild(title);
        content.appendChild(counter);

        card.appendChild(img);
        card.appendChild(content);
        container.appendChild(card);
    }
}