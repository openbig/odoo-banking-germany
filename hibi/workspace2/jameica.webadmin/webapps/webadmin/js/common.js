function change_to(element) {
  element.prevClass = element.className;
  element.className = "highlight";
}

function change_back(element) {
  element.className = element.prevClass;
}
