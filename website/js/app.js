import axios from "axios";

var fileCounts = new Map();
var project_id = ""

function buildButtonClickHandler() {
    document.getElementById("click_to_show_preview_text").style.display = "none";
    document.getElementById("download-button-id").disabled = true;
    document.getElementById("build-button-id").disabled = true;
    document.getElementById("nesting_loader").style.display = "block";

    document.getElementById("preview_image").style.display = "none";

    let data = {};
    data['project_id'] = project_id;
    data['file_counts'] = fileCounts;
    data['plate_width'] = document.getElementById("bin_width").value;
    data['plate_height'] = document.getElementById("bin_height").value;

    fetch('http://localhost:8080/nest', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })
        .then(response => {
            if (!response.ok && response.status === 400) {
                return response.json().then(err => {
                    throw new Error(err.reason);
                });
            }
            return response.json();
        })
        .then(response => {
            console.log(response.id)
            document.getElementById("build-button-id").disabled = false;
            document.getElementById("download-button-id").disabled = false;
            document.getElementById("nesting_loader").style.display = "none";
            document.getElementById("click_to_show_preview_text").style.display = "none";

            document.getElementById("download-button-id").onclick = () => {
                download(response.id)
            }

            updatePreview(response.id)
        })
        .catch(error => {
            document.getElementById("preview_image").style.display = "none";
            document.getElementById("nesting_loader").style.display = "none";
            document.getElementById("build-button-id").disabled = false;

            let errorMessage = error.message || "Something went wrong. Please try again later.";
            document.getElementById("click_to_show_preview_text").style.display = "block";
            document.getElementById("click_to_show_preview_text").textContent = errorMessage;
        })
}

function download(nestedId) {
    axios({
        url: `http://localhost:8080/nested/${nestedId}?format=dxf`,
        method: 'GET',
        responseType: 'blob'
    })
        .then((response) => {
            const url = window.URL
                .createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'nested.dxf');
            document.body.appendChild(link);
            link.click();
        })
}

document.addEventListener('DOMContentLoaded', () => {
    fetch('http://localhost:8080/project')
        .then(response => response.json())
        .then(data => {
                project_id = data.id
                initProjectCard(data)
                initProjectName(data)
            }
        )
        .catch(error => {
                window.location.href = '/500.html';
                console.error('Error:', error)
            }
        );

    document.querySelector('#build-button-id').addEventListener('click', () => {
        buildButtonClickHandler();
    })
});

function updatePreview(nestedId) {
    const img = document.getElementById("preview_image");
    img.style.display = "block";
    img.src = `http://localhost:8080/nested/${nestedId}?format=svg`;
    img.alt = 'SVG preview image';
}

function initProjectName(projects) {
    const container = document.querySelector('.project-title');
    const title = document.createElement('h1');
    title.textContent = projects.name;
    container.appendChild(title);
}

function adjustCount(input, fileKey, counterInput, increment) {
    const currentValue = parseInt(input.textContent, 10) || 0;
    const newValue = currentValue + increment;
    if ((newValue < 0) && (increment < 0)) {
        return;
    }
    input.textContent = newValue;
    fileCounts[fileKey] = newValue;
    console.log(fileCounts)
}

function initProjectCard(project) {
    const container = document.querySelector('.card-container');
    console.log(project)
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

        const counterInput = document.createElement('p');
        counterInput.type = 'number';
        counterInput.textContent = '0';
        fileCounts[fileKey] = 0;
        counterInput.className = 'counter-input';

        const decrementButton = document.createElement('button');
        decrementButton.textContent = '-';
        decrementButton.onclick = () => adjustCount(counterInput, fileKey, counterInput, -1);

        const incrementButton = document.createElement('button');
        incrementButton.textContent = '+';
        incrementButton.onclick = () => adjustCount(counterInput, fileKey, counterInput, 1);

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
