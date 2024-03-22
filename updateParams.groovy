job('example') {
    parameters {
        booleanParam('FLAG', true)
        choiceParam('OPTION',["1", "two", "three"])
    }
}