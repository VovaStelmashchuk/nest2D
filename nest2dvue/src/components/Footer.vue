<template>
    <footer class="footer">
        <nav class="footer__nav footer-nav">
            <div class="footer-nav__link footer-nav-link">
                <RouterLink
                    class="footer-nav-link__label footer-nav-link__label--terms"
                    to="/terms-and-conditions"
                >
                    Terms and Conditions
                </RouterLink>
            </div>
            <div
                class="footer-nav__link footer-nav-link footer-nav-link--github"
            >
                <a
                    class="footer-nav-link__label"
                    href="https://github.com/VovaStelmashchuk/nest2D"
                    target="_blank"
                >
                    Github
                </a>
            </div>
        </nav>
        <div class="footer__copy footer-copy">
            <p class="footer-copy__label">
                © 2024 Vova Stelmashchuk. All rights reserved.
            </p>
        </div>
    </footer>
</template>

<script>
export default {
    name: 'Footer',
};
</script>

<style lang="scss" scoped>
.footer {
    padding: 20px;
    display: flex;
    align-items: center;

    &__nav {
        flex-grow: 1;
    }
    &__copy {
        width: calc(100% / 3);
    }
}
.footer-nav {
    display: flex;

    &__link {
        width: 50%;
    }
}
.footer-nav-link {
    &--github {
        text-align: center;
    }
    &__label {
        display: inline-block;

        &--terms {
            color: green;
        }
    }
}
.footer-copy {
    text-align: right;

    &__label {
        font-size: 14px;
        color: #666;
    }
}
</style>
