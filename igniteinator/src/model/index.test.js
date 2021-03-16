import { CARD_FIELDS, getCardsSorted, getFullImageUrl, testables } from "./";
import SortSpec from "./SortSpec";

const { sort } = testables;

const sortSpecs = {
    byNameAsc: new SortSpec(CARD_FIELDS.NAME, SortSpec.ASC),
    byNameDesc: new SortSpec(CARD_FIELDS.NAME, SortSpec.DESC),
    byCostAsc: new SortSpec(CARD_FIELDS.COST, SortSpec.ASC),
    byCostDesc: new SortSpec(CARD_FIELDS.COST, SortSpec.DESC)
}

const sortCards = {
    a1: { name: "a", cost: 1 },
    b1: { name: "b", cost: 1 },
    a2: { name: "a", cost: 2 },
    b2: { name: "b", cost: 2 },
}

test("sort empty array = empty", () => {
    expect(sort([], [])).toStrictEqual([])
});

test("sort array no spec = not changed", () => {
    expect(sort([sortCards.b2, sortCards.a1], []))
        .toStrictEqual([sortCards.b2, sortCards.a1]);
    expect(sort([sortCards.a1, sortCards.b2], []))
        .toStrictEqual([sortCards.a1, sortCards.b2]);
});

test("sort array single spec", () => {
    expect(sort([sortCards.a2, sortCards.b1], [sortSpecs.byNameAsc]))
        .toStrictEqual([sortCards.a2, sortCards.b1]);
    expect(sort([sortCards.b1, sortCards.a2], [sortSpecs.byNameAsc]))
        .toStrictEqual([sortCards.a2, sortCards.b1]);
    expect(sort([sortCards.a2, sortCards.b1], [sortSpecs.byNameDesc]))
        .toStrictEqual([sortCards.b1, sortCards.a2]);
    expect(sort([sortCards.b1, sortCards.a2], [sortSpecs.byNameDesc]))
        .toStrictEqual([sortCards.b1, sortCards.a2]);
});

test("sort array multi spec", () => {
});
