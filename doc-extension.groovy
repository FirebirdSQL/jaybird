docinfo_processor {
    document -> {
        if (!document.isBasebackend('html') || !document.hasAttribute('fb-canonical-html')) {
            return
        }
        return "<link rel=\"canonical\" href=\"${document.getAttribute('fb-canonical-html')}\"/>"
    }
}
