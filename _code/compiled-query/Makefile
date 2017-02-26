all: copy

svg:
	cat querytree.dot | dot -Tsvg > querytree.svg
copy: svg
	cp querytree.svg ../../images/querytree.svg
