fmt:
	cljstyle check

lint:
	clj-kondo --lint src test

# run-dev:
# 	clj -X:dev

main:
	clj -M:run dev

test:
	clj -X:test
