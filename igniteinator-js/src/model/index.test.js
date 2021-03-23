import { CARD_FIELDS, getCardsSorted, getFullImageUrl, testables } from "./";
import SortSpec from "./SortSpec";

const { sort, filter } = testables;

const sortSpecs = {
    byNameAsc: SortSpec.fromField(CARD_FIELDS.NAME, SortSpec.ASC),
    byNameDesc: SortSpec.fromField(CARD_FIELDS.NAME, SortSpec.DESC),
    byCostAsc: SortSpec.fromField(CARD_FIELDS.COST, SortSpec.ASC),
    byCostDesc: SortSpec.fromField(CARD_FIELDS.COST, SortSpec.DESC)
}

// Generate {a1:{name:"a",cost:1} ... }
const cards = ["a", "b", "c", "d"].reduce((resS, curS) => {
    return {
        ...resS,
        ...[1, 2, 3, 4].reduce((resN, curN) => {
            return {
                ...resN,
                ...{
                    [curS + curN]: {
                        name: curS,
                        cost: curN
                    }
                }
            }
        }, {})
    }
}, {});

const cardsArray = Object.keys(cards).map(k => cards[k]);

test("sort empty array = empty", () =>
    expect(sort([], [])).toStrictEqual([]));

test("sort array no spec = not changed", () => {
    expect(sort([cards.b2, cards.a1], []))
        .toStrictEqual([cards.b2, cards.a1]);
    expect(sort([cards.a1, cards.b2], []))
        .toStrictEqual([cards.a1, cards.b2]);
});

test("sort array single spec", () => {
    expect(sort([cards.a2, cards.b1], [sortSpecs.byNameAsc]))
        .toStrictEqual([cards.a2, cards.b1]);
    expect(sort([cards.b1, cards.a2], [sortSpecs.byNameAsc]))
        .toStrictEqual([cards.a2, cards.b1]);
    expect(sort([cards.a2, cards.b1], [sortSpecs.byNameDesc]))
        .toStrictEqual([cards.b1, cards.a2]);
    expect(sort([cards.b1, cards.a2], [sortSpecs.byNameDesc]))
        .toStrictEqual([cards.b1, cards.a2]);
});

test("sort array multi spec", () =>
    expect(sort([cards.b1, cards.b2, cards.a1, cards.a2], [
        sortSpecs.byNameAsc, sortSpecs.byCostDesc]))
        .toStrictEqual([cards.a2, cards.a1, cards.b2, cards.b1]));

test("filter empty filter", () =>
    expect(filter(cardsArray, []))
        .toStrictEqual(cardsArray)
);

test("filter one filter", () =>
    expect(filter(cardsArray, [c => c.cost === 1]))
        .toStrictEqual([cards.a1, cards.b1, cards.c1, cards.d1]));

test("filter multi filter", () =>
    expect(filter(cardsArray,
        [c => c.name === "a",
        c => c.cost > 1,
        c => c.cost < 4]))
        .toStrictEqual([cards.a2, cards.a3]));
