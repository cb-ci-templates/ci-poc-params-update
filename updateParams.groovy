job('example') {
    parameters {
        booleanParam('FLAG', true)
        choiceParam('OPTION', ${params})
    }
}