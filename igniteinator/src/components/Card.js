import CardImage from "./CardImage"

const Card = ({ card }) => (
    <div>
        <div>{card.name}</div>
        <div>{card.cost}</div>
        <div><CardImage src={card.image} /></div>
    </div>
);

export default Card;
