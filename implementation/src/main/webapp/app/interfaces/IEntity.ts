export interface IEntity {
    label: string;
    id: string;
    URI: string;
    source: string;
    classes: string;
    score: number;
    
    description?: string;
    imageUrl?: string;
    isInstance?: boolean;
}