<template>
    <div class="blog">

        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Big update, #9, My summer vacation (2)
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-05-08">Aug 09, 2024</time>
                by nest2d
            </p>
            <h2>New feature</h2>
            <ul class="blog-container__list">
                <p>Add support for Arc DXF tag</p>
            </ul>
            <h2>Tech task</h2>
            <ul class="blog-container__list">
                <li>Migrate to jagua-rs</li>
                <li>Remove postgress sql</li>
                <li>Remove the old nest algorithm</li>
                <li>Add mongo for nested history</li>
                <li>Implement save dxf and svg files to minio</li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>Nested history page</li>
                <li>More DXF tags supports</li>
            </ul>

            <h2>Thanks</h2>
            <p>Thanks to JeroenGar for the great project, I hope it will be useful for me and for the community</p>
            <p>Thanks to anyone who uses the project, and uploads the project to the platform. It helps be with development</p>
        </div>

        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Big update, #8, My summer vacation
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-05-08">Aug 05, 2024</time>
                by nest2d
            </p>
            <h2>New feature</h2>
            <ul class="blog-container__list">
                <p>Not new features, but a lot of changes under the hood. I found a project (jaguar-rs)[https://github.com/JeroenGar/jagua-rs], make a fork and wrap the origin repo
                as a rest api service.</p>
            </ul>
            <h2>Tech task</h2>
            <ul class="blog-container__list">
                <li>Migrate to jagua-rs</li>
                <li>Remove postgress sql</li>
                <li>Remove the old nest algorithm</li>
                <li>Add mongo for nested history</li>
                <li>Implement save dxf and svg files to minio</li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>Nested history page</li>
                <li>More DXF tags supports</li>
            </ul>

            <h2>Thanks</h2>
            <p>Thanks to JeroenGar for the great project, I hope it will be useful for me and for the community</p>
            <p>Thanks to anyone who uses the project, and uploads the project to the platform. It helps be with development</p>
        </div>

        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Project updates, #7, More DXF tag
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-30-03">Mar 30, 2024</time>
                by nest2d
            </p>
            <h2>New feature</h2>
            <ul class="blog-container__list">
                <li>Add support for more DXF tags:</li>
                <li>- Add support for circle tag</li>
                <li>- Add support for circle Polyline</li>
            </ul>
            <h2>Fun news</h2>
            <p>The project has the first organic upload, thank you!!!</p>
            <a href="https://nest2d.online/project/nome">Project here</a>
            <p>
                The bugs fixes for support the project. The project was fixed by
                my self, in future DXF will works fine.
            </p>
            <h2>Tech task</h2>
            <ul class="blog-container__list">
                <li>Implement CI for the backend project</li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>Implement the nested history page</li>
                <li>Add support for more DXF tags</li>
            </ul>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Project updates, #6, Uploads from the web
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-17-03">Mar 17, 2024</time>
                by nest2d
            </p>
            <h2>New feature</h2>
            <ul class="blog-container__list">
                <li>Implement creating project from webpage, user friendly</li>
            </ul>
            <h2>Tech task</h2>
            <ul class="blog-container__list">
                <li>Try to make CI for the backend app. (in progress)</li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>Implement the nested history page</li>
                <li>Finish CI for backend app</li>
                <li>Add support for more DXF tags</li>
            </ul>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Project updates, #5, I learn how to do the database, also CI
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-08-03">Mar 8, 2024</time>
                by nest2d
            </p>
            <h2>New feature</h2>
            <ul class="blog-container__list">
                <li>Move max nesting time to config</li>
            </ul>
            <h2>Bug fix</h2>
            <ul class="blog-container__list">
                <li>Fix the database query</li>
                <li>Speed up the new NFP cache</li>
            </ul>
            <h2>Tech task</h2>
            <ul class="blog-container__list">
                <li>Make the CI which build docker image of the web app</li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>Add CI for backend app</li>
                <li>Add support for more DXF tags</li>
                <li>Make possible to and new project for end user</li>
            </ul>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Project updates, #4, We have the database
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-27-02">Feb 27, 2024</time>
                by nest2d
            </p>
            <h2>New features</h2>
            <ul class="blog-container__list">
                <li>Implement data base</li>
                <li>Move NFP pairs cache to postgres database</li>
                <li>
                    Move all json file base storage to postgres SQL, like a good
                    developers in real app
                </li>
                <li>Move all path to files into database</li>
                <li>
                    Add the progress bar to web page. I hope it's improve the
                    user experience
                </li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>
                    Implement CI for deploy docker container, for web and
                    backend
                </li>
                <li>Better look for holes into preview images</li>
                <li>Speed up the new NFP cache</li>
                <li>Add support for more DXF tags</li>
            </ul>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">
                Project updates, #3, We have the cache
            </h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-18-02">Feb 18, 2024</time>
                by nest2d
            </p>
            <h2>New features</h2>
            <ul class="blog-container__list">
                <li>
                    Implement cache for NFP pairs, each nesting add new data to
                    cache and improve future nest quality
                </li>
                <li>Added the preview photo to the project card</li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>
                    Implement CI for deploy docker container, for web and
                    backend
                </li>
                <li>Implement progress bar with timing or presented</li>
                <li>Better look for holes into preview images</li>
            </ul>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">Project updates, #2</h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-11-02">Feb 11, 2024</time>
                by nest2d
            </p>
            <h2>New features</h2>
            <ul class="blog-container__list">
                <li>
                    Fully support for LwPolyline, (includes code 42 for bulge)
                </li>
                <li>
                    Implement combine paths (currently not fully tested with
                    combine path from different DXF types)
                </li>
            </ul>
            <h2>New projects</h2>
            <ul class="blog-container__list">
                <li>
                    Laser gridfinity boxes by OpenSCAD (the project with Line)
                </li>
                <li>
                    Big box, my big box for Laser gridfinity (the project proof
                    the curse and inner hole works fine)
                </li>
            </ul>
            <h2>Next stages</h2>
            <ul class="blog-container__list">
                <li>
                    Implement CI for deploy docker container, for web and
                    backend
                </li>
                <li>Add the photo to the project</li>
                <li>Better look for holes into preview images</li>
            </ul>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">Project updates, #1</h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-08-02">Feb 8, 2024</time>
                by nest2d
            </p>
            <p>
                The project migrate to vue.js for implementation navigation
                inside SPA
            </p>
            <h2>Add features</h2>
            <p>Project list at main page</p>
            <p>New UI (based on vue.js template)</p>
            <h2>
                Currently the project support the following DXF entity types
            </h2>
            <p>LwPolyline</p>
            <p>Line</p>
            <h2>Next stages</h2>
            <p>
                Add project with Line (getting the laser cutting gridfinity from
                open scad)
            </p>
            <p>Implement CI for deploy docker container</p>
            <p>Implement base support for shapes with holes</p>
            <p>Add the photo to the project</p>
        </div>
        <div class="blog__container blog-container">
            <h2 class="blog-container__title">The start of project</h2>
            <p class="blog-container__meta">
                Published on
                <time datetime="2024-08-01">Feb 8, 2024</time>
                by nest2d
            </p>
            <h2>
                The online platform for Nest algorithm. What is Nest Problem?
            </h2>
            <p>
                Given a square piece of material and some letters to be
                laser-cut: We want to pack all the letters into the square,
                using as little material as possible. If a single square is not
                enough, we also want to minimize the number of squares used. In
                the CNC world this is called "nesting", and software that does
                this is typically targeted at industrial customers and very
                expensive.
            </p>
            <h2>Project goal</h2>
            <p>
                The project has a goal to create a fully open source software
                for nesting irregular shapes into square
            </p>
            <p>
                The second goal is creating a platform for maker. To share laser
                cutting project with community. And provide the best user
                experience, which will achieve by features like a online nesting
                tool.
            </p>
            <h2>Help the project</h2>
            <p>
                Visit our github repo to suggest any issue or pull request
                <a href="https://github.com/vovastelmashchuk/nest2d">Nest2d</a>
            </p>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.blog {
    color: var(--color-text);

    &__container {
        max-width: 840px;
        margin-left: auto;
        margin-right: auto;
    }
}
.blog-container {
    padding: 20px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    line-height: 1.6;

    @media (max-width: 900px) {
        padding: 10px;
    }

    &__title {
        font-size: 2.5rem;
        font-weight: bold;

        @media (max-width: 900px) {
            font-size: 2rem;
        }
    }
    &__list {
        list-style-type: disc;
        padding-inline-start: 40px;
    }
    &__meta {
        font-size: 0.9rem;
        color: #666;
    }
    & > * {
        margin-bottom: 20px;
    }
}
</style>
