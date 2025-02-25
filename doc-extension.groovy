// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
docinfo_processor {
    document -> {
        if (!document.isBasebackend('html') || !document.hasAttribute('fb-canonical-html')) {
            return
        }
        return "<link rel=\"canonical\" href=\"${document.getAttribute('fb-canonical-html')}\"/>"
    }
}
