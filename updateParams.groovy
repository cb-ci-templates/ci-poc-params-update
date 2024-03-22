def arrayList = new ArrayList($params)
printl arrayList
job('example') {
    parameters {
        booleanParam('FLAG', true)
        choiceParam('OPTION',arrayList)
    }
}