<template>
    <div class="article-container">
        <div class="article-title">
            <p>Project updates, #3, We have the cache</p>
        </div>
        <div class="article-meta">
            <p>Published on
                <time datetime="2024-18-02">Feb 18, 2024</time>
                by nest2d
            </p>
        </div>
        <div class="article-body">
            <h2>New features</h2>
            <ul>
                <li>Implement cache for NFP pairs, each nesting add new data to cache and improve future nest quality
                </li>
                <li>Added the preview photo to the project card</li>
            </ul>
            <h2>Next stages</h2>
            <ul>
                <li>Implement CI for deploy docker container, for web and backend</li>
                <li>Implement progress bar with timing or presented</li>
                <li>Better look for holes into preview images</li>
            </ul>
            <p></p>
        </div>
    </div>
    <div class="article-container">
        <div class="article-title">
            <p>Project updates, #2</p>
        </div>
        <div class="article-meta">
            <p>Published on
                <time datetime="2024-11-02">Feb 11, 2024</time>
                by nest2d
            </p>
        </div>
        <div class="article-body">
            <h2>New features</h2>
            <ul>
                <li>Fully support for LwPolyline, (includes code 42 for bulge)</li>
                <li>Implement combine paths (currently not fully tested with combine path from different DXF types)</li>
            </ul>

            <h2>New projects</h2>
            <ul>
                <li>Laser gridfinity boxes by OpenSCAD (the project with Line)</li>
                <li>Big box, my big box for Laser gridfinity (the project proof the curse and inner hole works fine)
                </li>
            </ul>
            <h2>Next stages</h2>
            <ul>
                <li>Implement CI for deploy docker container, for web and backend</li>
                <li>Add the photo to the project</li>
                <li>Better look for holes into preview images</li>
            </ul>
            <p></p>
        </div>
    </div>
    <div class="article-container">
        <div class="article-title">
            <p>Project updates, #1</p>
        </div>
        <div class="article-meta">
            <p>Published on
                <time datetime="2024-08-02">Feb 8, 2024</time>
                by nest2d
            </p>
        </div>
        <div class="article-body">
            <p>The project migrate to vue.js for implementation navigation inside SPA</p>
            <h2>Add features</h2>
            <p>Project list at main page</p>
            <p>New UI (based on vue.js template)</p>
            <h2>Currently the project support the following DXF entity types</h2>
            <p>LwPolyline</p>
            <p>Line</p>
            <h2>Next stages</h2>
            <p>Add project with Line (getting the laser cutting gridfinity from open scad)</p>
            <p>Implement CI for deploy docker container</p>
            <p>Implement base support for shapes with holes</p>
            <p>Add the photo to the project</p>
            <p></p>
        </div>
    </div>
    <div class="article-container">
        <div class="article-title">
            <p>The start of project</p>
        </div>
        <div class="article-meta">
            <p>Published on
                <time datetime="2024-08-01">Feb 8, 2024</time>
                by nest2d
            </p>
        </div>
        <div class="article-body">
            <h2>The online platform for Nest algorithm. What is Nest Problem?</h2>
            <p>Given a square piece of material and some letters to be laser-cut:
                We want to pack all the letters into the square, using as little material as possible. If a single
                square is not
                enough, we also want to minimize the number of squares used.
                In the CNC world this is called "nesting", and software that does this is typically targeted at
                industrial
                customers and very expensive.</p>
            <h2>Project goal</h2>
            <p>The project has a goal to create a fully open source software for nesting irregular shapes into
                square</p>
            <p>The second goal is creating a platform for maker. To share laser cutting project with community. And
                provide the best user experience, which will achieve by features like a online nesting tool.</p>
            <h2>Help the project</h2>
            <p>Visit our github repo to suggest any issue or pull request <a
                href="https://github.com/vovastelmashchuk/nest2d">Nest2d</a></p>
        </div>
    </div>
</template>

<style scoped>
.article-container {
    max-width: 800px; /* Sets a maximum width to ensure readability */
    margin: 4px auto; /* Centers the article */
    padding: 20px; /* Adds padding around the article */
    background-color: var(--color-background); /* Article background color */
    color: #333; /* Article text color */
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* Adds subtle shadow for depth */
    border-radius: 5px; /* Rounds the corners */
    line-height: 1.6; /* Improves readability */
}

.article-title {
    font-size: 2.5rem; /* Title font size */
    margin-bottom: 20px; /* Space below the title */
    color: var(--color-text); /* Title text color */
    font-weight: bold; /* Makes the title bold */
}

.article-meta {
    font-size: 0.9rem; /* Metadata font size (e.g., publication date, author) */
    color: #666; /* Metadata text color */
    margin-bottom: 20px; /* Space below the metadata */
}

.article-body {
    font-size: 1rem; /* Body text font size */
}

.article-body h2,
.article-body h3 {
    margin-top: 20px; /* Space above headings */
    margin-bottom: 10px; /* Space below headings */
    color: var(--color-text); /* Title text color */
    overflow-wrap: break-word;
    white-space: normal;
}

.article-body p {
    margin-bottom: 20px; /* Space below paragraphs */
    color: var(--color-text);
    overflow-wrap: break-word;
    white-space: normal;
}

.article-body a {
    color: #007bff; /* Link color */
    text-decoration: none; /* Removes underline from links */
    overflow-wrap: break-word;
    white-space: normal;
}

.article-body a:hover {
    text-decoration: underline; /* Adds underline on hover for links */
}

.article-body img {
    max-width: 100%; /* Ensures images are not wider than the article */
    height: auto; /* Keeps the original aspect ratio */
    display: block; /* Makes images block level to center */
    margin: 0 auto 20px; /* Centers images and adds space below */
}

.article-footer {
    font-size: 0.9rem; /* Footer text font size */
    color: var(--color-text); /* Title text color */
    border-top: 1px solid #eee; /* Adds a line above the footer */
    padding-top: 20px; /* Space above the footer content */
    margin-top: 20px; /* Space above the line */
}

.article-container ul {
    margin-bottom: 20px;
    color: var(--color-text);
    overflow-wrap: break-word;
    white-space: normal;
}

/* Responsive design adjustments */
@media (max-width: 900px) {
    .article-container {
        padding: 10px; /* Reduces padding on smaller screens */
    }

    .article-title {
        font-size: 2rem; /* Reduces title size on smaller screens */
    }
}
</style>
<script setup>
</script>
